package org.workcraft.gui;

import java.net.URI;
import java.net.URISyntaxException;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.util.GUI;

public class MainWindowActions {
    public static final Action CREATE_WORK_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().createWork();
            } catch (OperationCancelledException e) { }
        }
        @Override public String getText() {
            return "Create work...";
        }
    };
    public static final Action OPEN_WORK_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().openWork();
            } catch (OperationCancelledException e) { }
        }
        @Override public String getText() {
            return "Open work...";
        }
    };
    public static final Action MERGE_WORK_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().mergeWork();
            } catch (OperationCancelledException e) { }
        }
        @Override public String getText() {
            return "Merge work...";
        }
    };
    public static final Action SAVE_WORK_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().saveWork();
            } catch (OperationCancelledException e) {
            }
        }
        @Override public String getText() {
            return "Save work";
        }
    };
    public static final Action SAVE_WORK_AS_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().saveWorkAs();
            } catch (OperationCancelledException e) {
            }
        }
        public String getText() {
            return "Save work as...";
        }
    };
    public static final Action CLOSE_ACTIVE_EDITOR_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().closeActiveEditor();
            } catch (OperationCancelledException e) {
            }
        }
        public String getText() {
            return "Close active work";
        }
    };

    public static final Action CLOSE_ALL_EDITORS_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getMainWindow().closeEditorWindows();
            } catch (OperationCancelledException e) {
            }
        }
        public String getText() {
            return "Close all works";
        }
    };

    public static final Action RECONFIGURE_PLUGINS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Reconfigure plugins";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.getPluginManager().reconfigure(true);
            } catch (PluginInstantiationException e) {
                e.printStackTrace();
            }
        }
    };

    public static final Action SHUTDOWN_GUI_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            try {
                f.shutdownGUI();
            } catch (OperationCancelledException e) { }
        }
        public String getText() {
            return "Switch to console mode";
        }
    };

    public static final Action EXIT_ACTION = new Action() {
        @Override public void run() {
            final Framework f = Framework.getInstance();
            f.shutdown();
        }
        public String getText() {
            return "Exit";
        }
    };

    public static final Action IMPORT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Import...";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().importFrom();
        }
    };

    public static final Action EDIT_UNDO_ACTION = new Action() {
        @Override
        public String getText() {
            return "Undo";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().undo();
        }
    };

    public static final Action EDIT_REDO_ACTION = new Action() {
        @Override
        public String getText() {
            return "Redo";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().redo();
        }
    };

    public static final Action EDIT_CUT_ACTION = new Action() {
        @Override
        public String getText() {
            return "Cut";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().cut();
        }
    };

    public static final Action EDIT_COPY_ACTION = new Action() {
        @Override
        public String getText() {
            return "Copy";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().copy();
        }
    };

    public static final Action EDIT_PASTE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Paste";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().paste();
        }
    };

    public static final Action EDIT_DELETE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Delete";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().delete();
        }
    };

    public static final Action EDIT_SELECT_ALL_ACTION = new Action() {
        @Override
        public String getText() {
            return "Select all";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().selectAll();
        }
    };

    public static final Action EDIT_SELECT_INVERSE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Inverse selection";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().selectInverse();
        }
    };

    public static final Action EDIT_SELECT_NONE_ACTION = new Action() {
        @Override
        public String getText() {
            return "Deselect";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().selectNone();
        }
    };

    public static final Action EDIT_SETTINGS_ACTION = new Action() {
        @Override
        public String getText() {
            return "Preferences...";
        }
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().editSettings();
        }
    };

    public static final Action VIEW_ZOOM_IN = new Action() {
        @Override
        public String getText() {
            return "Zoom in";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.zoomIn();
            }
        }
    };

    public static final Action VIEW_ZOOM_OUT = new Action() {
        @Override
        public String getText() {
            return "Zoom out";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.zoomOut();
            }
        }
    };

    public static final Action VIEW_ZOOM_DEFAULT = new Action() {
        @Override
        public String getText() {
            return "Default zoom";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.zoomDefault();
            }
        }
    };

    public static final Action VIEW_PAN_CENTER = new Action() {
        @Override
        public String getText() {
            return "Center selection";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.panCenter();
            }
        }
    };

    public static final Action VIEW_ZOOM_FIT = new Action() {
        @Override
        public String getText() {
            return "Fit selection to screen";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.zoomFit();
            }
        }
    };

    public static final Action VIEW_PAN_LEFT = new Action() {
        @Override
        public String getText() {
            return "Pan left";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.panLeft();
            }
        }
    };

    public static final Action VIEW_PAN_UP = new Action() {
        @Override
        public String getText() {
            return "Pan up";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.panUp();
            }
        }
    };

    public static final Action VIEW_PAN_RIGHT = new Action() {
        @Override
        public String getText() {
            return "Pan right";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.panRight();
            }
        }
    };

    public static final Action VIEW_PAN_DOWN = new Action() {
        @Override
        public String getText() {
            return "Pan down";
        }
        @Override
        public void run() {
            GraphEditor editor = Framework.getInstance().getMainWindow().getCurrentEditor();
            if (editor != null) {
                editor.panDown();
            }
        }
    };

    public static final Action RESET_GUI_ACTION = new Action() {
        @Override
        public String getText() {
            return "Reset UI layout";
        }

        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().resetLayout();
        }

    };

    public static final Action HELP_OVERVIEW_ACTION = new Action() {
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().openExternally("overview/start.html", "Overview access error");
        }

        public String getText() {
            return "Overview";
        }
    };

    public static final Action HELP_CONTENTS_ACTION = new Action() {
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().openExternally("help/start.html", "Help access error");
        }

        public String getText() {
            return "Help contents";
        }
    };

    public static final Action HELP_TUTORIALS_ACTION = new Action() {
        @Override
        public void run() {
            final Framework f = Framework.getInstance();
            f.getMainWindow().openExternally("tutorial/start.html", "Tutorials access error");
        }

        public String getText() {
            return "Tutorials";
        }
    };

    public static final Action HELP_BUGREPORT_ACTION = new Action() {
        @Override
        public void run() {
            URI uri;
            try {
                uri = new URI("https://github.com/tuura/workcraft/issues/new");
                DesktopApi.browse(uri);
            } catch (URISyntaxException e) {
                System.out.println(e);
            }
        }

        public String getText() {
            return "Report a bug at GitHub";
        }
    };

    public static final Action HELP_EMAIL_ACTION = new Action() {
        @Override
        public void run() {
            URI uri;
            try {
                uri = new URI("mailto", "support@workcraft.org", null);
                DesktopApi.browse(uri);
            } catch (URISyntaxException e) {
                System.out.println(e);
            }
        }

        public String getText() {
            return "Contact developers by e-mail";
        }
    };

    public static final Action HELP_ABOUT_ACTION = new Action() {
        @Override
        public void run() {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            AboutDialog about = new AboutDialog(mainWindow);
            GUI.centerToParent(about, mainWindow);
            about.setVisible(true);
        }

        public String getText() {
            return "About Workcraft";
        }
    };

}
