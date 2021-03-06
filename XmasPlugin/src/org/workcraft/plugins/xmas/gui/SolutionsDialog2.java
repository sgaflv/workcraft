package org.workcraft.plugins.xmas.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SolutionsDialog2 extends JDialog {

    public SolutionsDialog2(int n, String str) {

        JPanel contents = new JPanel();
        contents.setLayout(new BorderLayout());

        if (n == 1) {
            JLabel label = new JLabel("The system has a deadlock");
            contents.add(label, BorderLayout.NORTH);
        } else {
            JLabel label = new JLabel("Solution found");
            contents.add(label, BorderLayout.NORTH);
        }

        JPanel solutionsPanel = new JPanel();
        solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));

        solutionsPanel.add(new SolutionPanel(str, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SolutionsDialog2.this.setVisible(false);
            }
        }));

        contents.add(solutionsPanel, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SolutionsDialog2.this.setVisible(false);
            }
        });

        buttonsPanel.add(okButton);
        contents.add(buttonsPanel, BorderLayout.SOUTH);

        this.setContentPane(contents);
        //this.add(contents);
        this.pack();
        this.setVisible(true);
    }

}
