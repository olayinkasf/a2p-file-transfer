package desktop.olayinka.file.transfer.view;

import javax.swing.*;

/**
 * Created by Olayinka on 11/5/2015.
 */
public class WrappableJLabel extends JTextArea {

    double width;

    public WrappableJLabel(double width) {
        setWrapStyleWord(true);
        setLineWrap(true);
        setOpaque(false);
        setEditable(false);
        setFocusable(false);
        setBackground(UIManager.getColor("Label.background"));
        setFont(UIManager.getFont("Label.font"));
        setBorder(UIManager.getBorder("Label.border"));
    }

}
