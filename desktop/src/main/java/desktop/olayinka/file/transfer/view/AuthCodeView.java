package desktop.olayinka.file.transfer.view;

import com.olayinka.file.transfer.model.DeviceConnection;
import desktop.olayinka.file.transfer.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Olayinka on 11/2/2015.
 */
public class AuthCodeView extends JDialog {


    public static final String MESSAGE = "Enter the following code on your device %s";

    public AuthCodeView(Frame frame, DeviceConnection connection, boolean b) {
        super(frame, b);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setPreferredSize(AppSettings.CODE_DIALOG_DIMENS);
        setMinimumSize(AppSettings.CODE_DIALOG_DIMENS);
        setMaximumSize(AppSettings.CODE_DIALOG_DIMENS);

        Dimension dimension;

        dimension = new Dimension((int) AppSettings.CODE_DIALOG_DIMENS.getWidth() - 20, 30);
        JLabel messageLabel = new JLabel();
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.PLAIN, 16));
        messageLabel.setText("Authentication access code".toUpperCase());
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setPreferredSize(dimension);
        messageLabel.setMinimumSize(dimension);
        messageLabel.setMaximumSize(dimension);
        add(messageLabel);

        dimension = new Dimension(100, 30);
        messageLabel = new JLabel();
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.PLAIN, 12));
        messageLabel.setText("Device:");
        messageLabel.setPreferredSize(dimension);
        messageLabel.setMinimumSize(dimension);
        messageLabel.setMaximumSize(dimension);
        messageLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(messageLabel);

        dimension = new Dimension(170, 30);
        messageLabel = new JLabel();
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.BOLD, 12));
        messageLabel.setText(connection.getDevice().getDisplayName());
        messageLabel.setPreferredSize(dimension);
        messageLabel.setMinimumSize(dimension);
        messageLabel.setMaximumSize(dimension);
        messageLabel.setHorizontalAlignment(JLabel.LEFT);
        add(messageLabel);

        dimension = new Dimension(100, 30);
        messageLabel = new JLabel();
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.PLAIN, 12));
        messageLabel.setText("IP Address:");
        messageLabel.setPreferredSize(dimension);
        messageLabel.setMinimumSize(dimension);
        messageLabel.setMaximumSize(dimension);
        messageLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(messageLabel);

        dimension = new Dimension(170, 30);
        messageLabel = new JLabel();
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.BOLD, 12));
        messageLabel.setText(connection.getDevice().getLastKnownIp());
        messageLabel.setPreferredSize(dimension);
        messageLabel.setMinimumSize(dimension);
        messageLabel.setMaximumSize(dimension);
        messageLabel.setHorizontalAlignment(JLabel.LEFT);
        add(messageLabel);

        dimension = new Dimension((int) AppSettings.CODE_DIALOG_DIMENS.getWidth() - 20, 100);
        JLabel codeLabel = new JLabel();
        codeLabel.setFont(new Font(messageLabel.getFont().getName(), Font.BOLD, 60));
        codeLabel.setText(connection.getCode());
        codeLabel.setHorizontalAlignment(JLabel.CENTER);
        codeLabel.setPreferredSize(dimension);
        codeLabel.setMinimumSize(dimension);
        codeLabel.setMaximumSize(dimension);
        add(codeLabel);

        JButton doneButton = new JButton();
        doneButton.setText("  DONE  ");
        add(doneButton);

        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        setTitle(connection.getDevice().getDisplayName());
        setResizable(false);
        setBackground(AppSettings.APP_COLOR);
    }

}
