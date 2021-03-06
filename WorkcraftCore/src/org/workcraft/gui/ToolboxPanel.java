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

package org.workcraft.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.gui.graph.tools.ToolProvider;

@SuppressWarnings("serial")
public class ToolboxPanel extends JPanel implements ToolProvider, GraphEditorKeyListener {

    class ToolTracker {
        ArrayList<GraphEditorTool> tools = new ArrayList<>();
        int nextIndex = 0;

        public void addTool(GraphEditorTool tool) {
            tools.add(tool);
            nextIndex = 0;
        }

        public void reset() {
            nextIndex = 0;
        }

        public GraphEditorTool getNextTool() {
            GraphEditorTool ret = tools.get(nextIndex);
            setNext(nextIndex + 1);
            return ret;
        }

        private void setNext(int next) {
            if (next >= tools.size()) {
                next %= tools.size();
            }
            nextIndex = next;
        }

        public void track(GraphEditorTool tool) {
            setNext(tools.indexOf(tool) + 1);
        }
    }

    private final SelectionTool selectionTool;
    private final CommentGeneratorTool labelTool;
    private final ConnectionTool connectionTool;

    private GraphEditorTool selectedTool;

    private final HashSet<GraphEditorTool> tools = new HashSet<>();
    private final HashMap<GraphEditorTool, JToggleButton> buttons = new HashMap<>();
    private final HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<>();

    private final GraphEditorPanel editor;

    public ToolboxPanel(GraphEditorPanel editor) {
        this.editor = editor;
        this.setFocusable(false);

        selectionTool = new SelectionTool();
        labelTool = new CommentGeneratorTool();
        connectionTool = new ConnectionTool();
        selectedTool = null;

        setToolsForModel(editor.getModel());
    }

    public void addTool(final GraphEditorTool tool, boolean selected) {
        tools.add(tool);
        int hotKeyCode = tool.getHotKeyCode();
        if (hotKeyCode != -1) {
            ToolTracker tracker = hotkeyMap.get(hotKeyCode);
            if (tracker == null) {
                tracker = new ToolTracker();
                hotkeyMap.put(hotKeyCode, tracker);
            }
            tracker.addTool(tool);
        }
        if (tool.requiresButton()) {
            JToggleButton button = createToolButton(tool);
            buttons.put(tool, button);
        }
        if (selected) {
            selectTool(tool);
        }
    }

    public JToggleButton createToolButton(final GraphEditorTool tool) {
        JToggleButton button = new JToggleButton();

        button.setFocusable(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 0, 0, 0));

        Insets insets = button.getInsets();
        int iconSize = SizeHelper.getToolIconSize();
        int minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom);

        Icon icon = tool.getIcon();
        if (icon == null) {
            button.setText(tool.getLabel());
            button.setPreferredSize(new Dimension(120, minSize));
        } else {
            BufferedImage crop = new BufferedImage(iconSize, iconSize,
                    BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(button, crop.getGraphics(), (iconSize - icon.getIconWidth()) / 2, (iconSize - icon.getIconHeight()) / 2);
            button.setIcon(new ImageIcon(crop));
            button.setPreferredSize(new Dimension(minSize, minSize));
        }

        int hotKeyCode = tool.getHotKeyCode();
        if (hotKeyCode != -1) {
            button.setToolTipText("[" + Character.toString((char) hotKeyCode) + "] " + tool.getLabel());
        } else {
            button.setToolTipText(tool.getLabel());
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTool(tool);
            }
        });
        this.add(button);
        return button;
    }

    @SuppressWarnings("unchecked")
    public <T extends GraphEditorTool> T getToolInstance(Class<T> cls) {
        for (GraphEditorTool tool : tools) {
            if (cls == tool.getClass()) {
                return (T) tool;
            }
        }
        for (GraphEditorTool tool : tools) {
            if (cls.isInstance(tool)) {
                return (T) tool;
            }
        }
        return null;
    }

    public <T extends GraphEditorTool> T selectToolInstance(Class<T> cls) {
        final T tool = getToolInstance(cls);
        if (tool != null) {
            selectTool(tool);
            return (T) tool;
        }
        return null;
    }

    public void selectTool(GraphEditorTool tool) {
        if (selectedTool != null) {
            ToolTracker oldTracker = hotkeyMap.get(selectedTool.getHotKeyCode());
            if (oldTracker != null) {
                oldTracker.reset();
            }
            selectedTool.deactivated(editor);
            setToolButtonSelected(selectedTool, false);
        }

        ToolTracker tracker = hotkeyMap.get(tool.getHotKeyCode());
        if (tracker != null) {
            tracker.track(tool);
        }

        if (tool == selectedTool) {
            selectedTool.reactivated(editor);
        }
        selectedTool = tool;

        controlPanel.setTool(selectedTool, editor);
        setToolButtonSelected(selectedTool, true);
        selectedTool.setup(editor);
        selectedTool.activated(editor);
        editor.updatePropertyView();
        editor.repaint();
    }

    public void addCommonTools() {
        addTool(selectionTool, true);
        addTool(labelTool, false);
        addTool(connectionTool, false);
    }

    private void setToolsForModel(VisualModel model) {
        setLayout(new SimpleFlowLayout(5, 5));

        Class<? extends CustomToolsProvider> customTools = Annotations.getCustomToolsProvider(model.getClass());
        if (customTools == null) {
            addCommonTools();
        } else {
            boolean selected = true;
            CustomToolsProvider provider = null;
            try {
                provider = customTools.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (provider != null) {
                for (GraphEditorTool tool : provider.getTools()) {
                    addTool(tool, selected);
                    selected = false;
                }
            }
        }

        for (Class<?> cls : Annotations.getDefaultCreateButtons(model.getClass())) {
            NodeGeneratorTool tool = new NodeGeneratorTool(new DefaultNodeGenerator(cls));
            addTool(tool, false);
        }

        for (Class<? extends GraphEditorTool>  tool : Annotations.getCustomTools(model.getClass())) {
            try {
                addTool(tool.newInstance(), false);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        doLayout();
        this.repaint();
    }

    public GraphEditorTool getTool() {
        return selectedTool;
    }

    public void keyPressed(GraphEditorKeyEvent event) {
        if (!event.isAltDown() && !event.isMenuKeyDown() && !event.isShiftDown()) {
            int keyCode = event.getKeyCode();
            ToolTracker tracker = hotkeyMap.get(keyCode);
            if (tracker != null) {
                selectTool(tracker.getNextTool());
            } else {
                selectedTool.keyPressed(event);
            }
        } else {
            selectedTool.keyPressed(event);
        }
    }

    public void keyReleased(GraphEditorKeyEvent event) {
        selectedTool.keyReleased(event);
    }

    public void keyTyped(GraphEditorKeyEvent event) {
        selectedTool.keyTyped(event);
    }

    ToolInterfaceWindow controlPanel = new ToolInterfaceWindow();

    public ToolInterfaceWindow getControlPanel() {
        return controlPanel;
    }

    public void setToolButtonState(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setEnabled(state);
        }
    }

    public void setToolButtonSelected(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setSelected(state);
        }
    }
}
