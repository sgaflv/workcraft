/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.tools.CircuitLayoutTool;
import org.workcraft.plugins.circuit.tools.RoutingAnalyserTool;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Digital Circuit")
@ShortName("circuit")
@CustomTools(CircuitToolsProvider.class)
public class VisualCircuit extends AbstractVisualModel {

    RouterClient routingGrid = new RouterClient();

    private final Circuit circuit;

    public VisualCircuit(Circuit model, VisualGroup root) {
        super(model, root);
        circuit = model;
    }

    public VisualCircuit(Circuit model) throws VisualModelInstantiationException {
        super(model);
        circuit = model;
        try {
            createDefaultFlatStructure();
        } catch (final NodeCreationException e) {
            throw new VisualModelInstantiationException(e);
        }
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Connections are only valid between different objects.");
        }

        if (second instanceof VisualConnection) {
            throw new InvalidConnectionException("Merging connections is not allowed.");
        }

        if (second instanceof VisualComponent) {
            for (final Connection c : getConnections(second)) {
                if (c.getSecond() == second) {
                    throw new InvalidConnectionException("Only one connection is allowed as a driver.");
                }
            }
        }

        if (first instanceof VisualContact) {
            final Contact contact = ((VisualContact) first).getReferencedContact();
            if (contact.isInput() && !contact.isPort()) {
                throw new InvalidConnectionException("Inputs of components cannot be drivers.");
            }
        }

        if (second instanceof VisualContact) {
            final Contact contact = ((VisualContact) second).getReferencedContact();
            if (contact.isOutput() && !contact.isPort()) {
                throw new InvalidConnectionException("Outputs of the components cannot be driven.");
            }
            if (contact.isInput() && contact.isPort()) {
                throw new InvalidConnectionException("Inputs from the environment cannot be driven.");
            }
        }

        final HashSet<Contact> drivenSet = new HashSet<>();
        final Circuit circuit = (Circuit) getMathModel();
        Contact driver = null;
        if (first instanceof VisualConnection) {
            final VisualConnection firstConnection = (VisualConnection) first;
            driver = CircuitUtils.findDriver(circuit, firstConnection.getReferencedConnection(), true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstConnection.getReferencedConnection(), true));
            }
        } else if (first instanceof VisualComponent) {
            final VisualComponent firstComponent = (VisualComponent) first;
            driver = CircuitUtils.findDriver(circuit, firstComponent.getReferencedComponent(), true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstComponent.getReferencedComponent(), true));
            }
        }
        if (second instanceof VisualComponent) {
            final VisualComponent secondComponent = (VisualComponent) second;
            drivenSet.addAll(CircuitUtils.findDriven(circuit, secondComponent.getReferencedComponent(), true));
        }
        int outputPortCount = 0;
        for (final Contact driven : drivenSet) {
            if (driven.isOutput() && driven.isPort()) {
                outputPortCount++;
                if (outputPortCount > 1) {
                    throw new InvalidConnectionException("Fork on output ports is not allowed.");
                }
                if ((driver != null) && driver.isInput() && driver.isPort()) {
                    throw new InvalidConnectionException(
                            "Direct connection from input port to output port is not allowed.");
                }
            }
        }
        // Handle zero-delay components
        final Node firstParent = first.getParent();
        if (firstParent instanceof VisualFunctionComponent) {
            final VisualFunctionComponent firstComponent = (VisualFunctionComponent) firstParent;
            final Node secondParent = second.getParent();
            if (secondParent instanceof VisualFunctionComponent) {
                final VisualFunctionComponent secondComponent = (VisualFunctionComponent) secondParent;
                if (firstComponent.getIsZeroDelay() && secondComponent.getIsZeroDelay()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to each other.");
                }
            }
            if (second instanceof VisualContact) {
                final VisualContact secondContact = (VisualContact) second;
                if (firstComponent.getIsZeroDelay() && secondContact.isPort() && secondContact.isOutput()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to output ports.");
                }
            }
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection)
            throws InvalidConnectionException {
        validateConnection(first, second);
        if (first instanceof VisualConnection) {
            final VisualConnection connection = (VisualConnection) first;
            final Point2D splitPoint = connection.getSplitPoint();
            final LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection,
                    splitPoint);
            final LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection,
                    splitPoint);

            final Container container = (Container) connection.getParent();
            final VisualJoint joint = createJoint(container);
            joint.setPosition(splitPoint);
            remove(connection);

            final VisualConnection predConnection = connect(connection.getFirst(), joint);
            predConnection.copyStyle(connection);
            ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);

            final VisualConnection succConnection = connect(joint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);

            first = joint;
        }

        VisualCircuitConnection vConnection = null;
        if ((first instanceof VisualComponent) && (second instanceof VisualComponent)) {
            final VisualComponent vComponent1 = (VisualComponent) first;
            final VisualComponent vComponent2 = (VisualComponent) second;

            final Node vParent = Hierarchy.getCommonParent(vComponent1, vComponent2);
            final Container vContainer = (Container) Hierarchy.getNearestAncestor(vParent, new Func<Node, Boolean>() {
                @Override
                public Boolean eval(Node node) {
                    return (node instanceof VisualGroup) || (node instanceof VisualPage);
                }
            });
            if (mConnection == null) {
                final MathNode mComponent1 = vComponent1.getReferencedComponent();
                final MathNode mComponent2 = vComponent2.getReferencedComponent();
                mConnection = circuit.connect(mComponent1, mComponent2);
            }
            vConnection = new VisualCircuitConnection(mConnection, vComponent1, vComponent2);
            vConnection.setArrowLength(0.0);
            vContainer.add(vConnection);
        }
        return vConnection;
    }

    public String getMathName(VisualComponent component) {
        return getMathModel().getName(component.getReferencedComponent());
    }

    public Collection<VisualFunctionContact> getVisualFunctionContacts() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualFunctionContact.class);
    }

    public Collection<VisualFunctionComponent> getVisualFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualFunctionComponent.class);
    }

    public VisualFunctionContact getOrCreateContact(Container container, String name, IOType ioType) {
        // here "parent" is a container of a visual model
        if (name != null) {
            if (container == null) {
                container = getRoot();
            }
            for (final Node n : container.getChildren()) {
                if (n instanceof VisualFunctionContact) {
                    final VisualFunctionContact contact = (VisualFunctionContact) n;
                    final String contactName = getMathModel().getName(contact.getReferencedContact());
                    if (name.equals(contactName)) {
                        return contact;
                    }
                } // TODO: if found something else with that name, return null
                  // or exception?
            }
        }

        Direction direction = Direction.WEST;
        if (ioType == null) {
            ioType = IOType.OUTPUT;
        }
        if (ioType == IOType.OUTPUT) {
            direction = Direction.EAST;
        }

        final VisualFunctionContact vc = new VisualFunctionContact(new FunctionContact(ioType));
        vc.setDirection(direction);

        if (container instanceof VisualFunctionComponent) {
            final VisualFunctionComponent component = (VisualFunctionComponent) container;
            component.addContact(this, vc);
        } else {
            final Container mathContainer = NamespaceHelper.getMathContainer(this, getRoot());
            mathContainer.add(vc.getReferencedComponent());
            add(vc);
        }
        if (name != null) {
            circuit.setName(vc.getReferencedComponent(), name);
        }
        vc.setPosition(new Point2D.Double(0.0, 0.0));
        return vc;
    }

    public VisualJoint createJoint(Container container) {
        if (container == null) {
            container = getRoot();
        }
        final VisualJoint joint = new VisualJoint(new Joint());
        final Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        mathContainer.add(joint.getReferencedComponent());
        container.add(joint);
        return joint;
    }

    public Collection<VisualContact> getVisualPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualContact.class, new Func<VisualContact, Boolean>() {
            @Override
            public Boolean eval(VisualContact arg) {
                return arg.isPort();
            }
        });
    }

    public Collection<VisualContact> getVisualDrivers() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualContact.class, new Func<VisualContact, Boolean>() {
            @Override
            public Boolean eval(VisualContact arg) {
                return arg.isDriver();
            }
        });
    }

    public Collection<Environment> getEnvironments() {
        return Hierarchy.getChildrenOfType(getRoot(), Environment.class);
    }

    private WorkspaceEntry getWorkspaceEntry() {
        final Framework framework = Framework.getInstance();
        final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        return editor == null ? null : editor.getWorkspaceEntry();
    }

    @NoAutoSerialisation
    public File getEnvironmentFile() {
        File file = null;
        for (final Environment env : getEnvironments()) {
            file = env.getFile();
            final File base = env.getBase();
            if (base != null) {
                final String basePath = base.getPath().replaceAll("\\\\", "/");
                final String filePath = file.getPath().replaceAll("\\\\", "/");
                if (filePath.startsWith(basePath)) {
                    final WorkspaceEntry we = getWorkspaceEntry();
                    final File newBase = we == null ? null : we.getFile().getParentFile();
                    if (newBase != null) {
                        String relativePath = filePath.substring(basePath.length(), filePath.length());
                        while (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1, relativePath.length());
                        }
                        file = new File(newBase, relativePath);
                    }
                }
            }
            break;
        }
        return file;
    }

    @NoAutoSerialisation
    public void setEnvironmentFile(File value) {
        final Collection<Environment> environments = getEnvironments();
        File file = null;
        if (environments.size() == 1) {
            final Environment env = environments.iterator().next();
            file = env.getFile();
        }
        final boolean envChanged = ((file == null) && (value != null)) || ((file != null) && !file.equals(value));
        if (envChanged) {
            final WorkspaceEntry we = getWorkspaceEntry();
            we.saveMemento();
            we.setChanged(true);
            for (final Environment env : environments) {
                remove(env);
            }
            if (value != null) {
                final Environment env = new Environment();
                env.setFile(value);
                final File base = we.getFile().getParentFile();
                env.setBase(base);
                add(env);
            }
        }
    }

    @Override
    public void draw(Graphics2D g, Decorator decorator) {
        super.draw(g, decorator);
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final ToolboxPanel toolbox = mainWindow.getCurrentToolbox();
        final GraphEditorTool tool = toolbox.getTool();
        if (tool instanceof RoutingAnalyserTool) {

            routingGrid.registerObstacles(this);

            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
            final Viewport viewport = editor.getViewport();
            routingGrid.draw(g, viewport);
        }
    }

    @Override
    public AbstractLayoutTool getBestLayoutTool() {
        return new CircuitLayoutTool();
    }

    @Override
    public ModelProperties getProperties(Node node) {
        final ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.add(new EnvironmentFilePropertyDescriptor(this));
        } else if (node instanceof VisualFunctionContact) {
            final VisualFunctionContact contact = (VisualFunctionContact) node;
            final VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
            properties.add(props.getSetProperty(contact));
            properties.add(props.getResetProperty(contact));
        }
        return properties;
    }

}
