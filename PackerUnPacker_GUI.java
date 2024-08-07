import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.*;
import javax.swing.*;

public class PackerUnPacker_GUI {
    public static void main(String[] args) {
        new Login();
    }
}

class ClockLabel extends JLabel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private String type;
    private SimpleDateFormat sdf;

    public ClockLabel(String type) {
        this.type = type;
        setForeground(Color.green);
        
        switch (type) {
            case "date":
                sdf = new SimpleDateFormat("MMMM dd yyyy");
                setFont(new Font("sans-serif", Font.PLAIN, 12));
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
            case "time":
                sdf = new SimpleDateFormat("hh:mm:ss a");
                setFont(new Font("sans-serif", Font.PLAIN, 40));
                setHorizontalAlignment(SwingConstants.CENTER);
                break;
            case "day":
                sdf = new SimpleDateFormat("EEEE");
                setFont(new Font("sans-serif", Font.PLAIN, 16));
                setHorizontalAlignment(SwingConstants.RIGHT);
                break;
        }
        
        Timer t = new Timer(1000, this);
        t.start();
    }

    public void actionPerformed(ActionEvent ae) {
        setText(sdf.format(new Date()));
    }
}

class Template extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    JButton minimize, exit;

    public Template() {
        setSize(800, 600);
        setTitle("Packer-Unpacker");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(Color.lightGray);

        minimize = new JButton("-");
        minimize.addActionListener(this);
        top.add(minimize);

        exit = new JButton("X");
        exit.addActionListener(this);
        top.add(exit);

        add(top, BorderLayout.NORTH);

        JPanel header = new JPanel();
        header.setBackground(Color.white);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(new ClockLabel("date"));
        header.add(new ClockLabel("time"));
        header.add(new ClockLabel("day"));

        add(header, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == minimize) {
            setState(Frame.ICONIFIED);
        } else if (ae.getSource() == exit) {
            System.exit(0);
        }
    }
}

class Login extends Template {
    private static final long serialVersionUID = 1L;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private int attempts = 3;

    public Login() {
        setVisible(true);

        JPanel content = new JPanel();
        content.setBackground(new Color(0, 50, 120));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Packer Unpacker: Login");
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Century", Font.BOLD, 17));
        content.add(label);

        content.add(Box.createVerticalStrut(20));

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.white);
        usernameLabel.setFont(new Font("Century", Font.BOLD, 14));
        content.add(usernameLabel);

        usernameField = new JTextField(15);
        content.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.white);
        passwordLabel.setFont(new Font("Century", Font.BOLD, 14));
        content.add(passwordLabel);

        passwordField = new JPasswordField(15);
        content.add(passwordField);

        content.add(Box.createVerticalStrut(20));

        JButton submitButton = new JButton("SUBMIT");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        content.add(submitButton);

        add(content, BorderLayout.SOUTH);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.length() < 8 || password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Short username or password", "Error", JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            passwordField.setText("");
            return;
        }

        if (username.equals("Admin123") && password.equals("Admin123")) {
            new NextPage(username).setVisible(true);
            this.dispose();
        } else {
            attempts--;
            if (attempts == 0) {
                JOptionPane.showMessageDialog(this, "Number of attempts finished", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect login or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

class Packer {
    private String directory;

    public Packer(String directory) {
        this.directory = directory;
    }

    public void pack() throws IOException {
        String packName = "packed_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(packName))) {
            File dir = new File(directory);
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zipOut.putNextEntry(zipEntry);
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zipOut.write(bytes, 0, length);
                        }
                    }
                }
            }
        }
        System.out.println("Packed files into " + packName);
    }

    public void unpack(String zipFile) throws IOException {
        File destDir = new File(directory);
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
        System.out.println("Unpacked " + zipFile + " into " + directory);
    }
}

class NextPage extends Template {
    private static final long serialVersionUID = 1L;
    private String username;

    public NextPage(String username) {
        this.username = username;

        JPanel content = new JPanel();
        content.setBackground(new Color(0, 50, 120));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Welcome: " + username);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Century", Font.BOLD, 17));
        content.add(label);

        content.add(Box.createVerticalStrut(20));

        JButton packButton = new JButton("Pack Files");
        packButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                packFiles();
            }
        });
        content.add(packButton);

        JButton unpackButton = new JButton("Unpack Files");
        unpackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unpackFiles();
            }
        });
        content.add(unpackButton);

        add(content, BorderLayout.SOUTH);
    }

    private void packFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String directory = chooser.getSelectedFile().getPath();
            Packer packer = new Packer(directory);
            try {
                packer.pack();
                JOptionPane.showMessageDialog(this, "Files packed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error packing files", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void unpackFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Zip files", "zip"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String zipFile = chooser.getSelectedFile().getPath();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String directory = chooser.getSelectedFile().getPath();
                Packer packer = new Packer(directory);
                try {
                    packer.unpack(zipFile);
                    JOptionPane.showMessageDialog(this, "Files unpacked successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error unpacking files", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
