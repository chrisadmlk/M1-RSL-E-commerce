package mysecurity.certificate;

import javax.swing.*;
import java.awt.event.*;

public class CertificateDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea certificateDisplay;
    private JLabel okSignatureLabel;
    private JLabel okValidityLabel;

    private CertificateHandler certificateHandler;
    private boolean accepted = false;

    public boolean isAccepted() {
        return accepted;
    }

    public CertificateDialog(CertificateHandler certificateHandler) {
        this.certificateHandler = certificateHandler;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        certificateDisplay.setText(certificateHandler.gatherInformation());
        okSignatureLabel.setText(String.valueOf(certificateHandler.checkSignature()));
        okValidityLabel.setText(String.valueOf(certificateHandler.checkValidity()));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        accepted = true;
        dispose();
    }

    private void onCancel() {
        accepted = false;
        dispose();
    }

}
