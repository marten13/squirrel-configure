package nl.berg.squirrel_configure_xml;

import java.io.File;
import java.io.IOException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.berg.squirrel_configure_xml.domain.AliasConfig;
import nl.berg.squirrel_configure_xml.util.AliasUtil;
import nl.berg.squirrel_configure_xml.util.DocumentUtil;
import nl.berg.squirrel_configure_xml.util.DriverUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SquirrelConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirrelConfigurer.class);

    public static final String ALIAS_FILENAME = "SQLAliases23.xml";
    public static final String ALIAS_TREE_FILENAME = "SQLAliases23_treeStructure.xml";
    public static final String DRIVER_FILENAME = "SQLDrivers.xml";
    public static final String HOME_PATH = "C:\\WS\\Data\\Squirrel-sql-3.5.3\\Settings\\";

    private static File squirrelConfigHome;
    private static File selectedJsonFile;
    private static String selectedDriver;

    private static Document aliasDocument;
    private static File aliasFile;

    private static Document aliasTreeDocument;
    private static File aliasTreeFile;

    private static Document driverDocument;

    public static void main(String[] args) {
        final JFrame window = new JFrame("SquirrelSql Instellen");
        //squirrelConfigHome = file(HOME_PATH);

        JPanel squirrelConfig = new JPanel();
        squirrelConfig.setLayout(new BoxLayout(squirrelConfig, BoxLayout.Y_AXIS));

        addSquirrelHomeSelector(window, squirrelConfig);

        Button saveConfigButton = new Button();
        saveConfigButton.setLabel("Configuratie doorvoeren");

        saveConfigButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (squirrelConfigHome == null) {
                            JOptionPane.showMessageDialog(window,
                                    "Kies eerst een squirrel config folder", "Squirrel config home mist",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (selectedDriver == null) {
                            JOptionPane.showMessageDialog(window,
                                    "Kies eerst een driver uit de lijst", "Driver mist",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (selectedJsonFile == null) {
                        } else if (selectedDriver == null) {
                            JOptionPane.showMessageDialog(window,
                                    "Kies eerst een alias file", "Alias config mist",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            try {
                                AliasConfig aliasConfig = new ObjectMapper().readValue(selectedJsonFile, AliasConfig.class);
                                Element driverElement = DriverUtil.findDriverBijNaam(driverDocument, selectedDriver);
                                AliasUtil.addOrUpdateAlias(aliasDocument, aliasTreeDocument, aliasConfig,
                                        DriverUtil.getDriverUUID(driverElement),
                                        DriverUtil.getDriverJdbcUrl(driverElement));
                                DocumentUtil.writeDocument(aliasDocument, aliasFile);
                                DocumentUtil.writeDocument(aliasTreeDocument, aliasTreeFile);

                                JOptionPane.showMessageDialog(window,
                                        "Alias '" + aliasConfig.getAliasName() + "' Toegevoegd", "Alias toegevoegd",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(window,
                                        "Fout bij uitlezen driverConfig: ", "Alias config fout",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
        squirrelConfig.add(saveConfigButton);

        window.setContentPane(squirrelConfig);
        window.setSize(600, 400);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    public static void addSquirrelHomeSelector(final Window window, final JPanel jPanel) {
        final JPanel squirrelHomeSelector = new JPanel();
        squirrelHomeSelector.setLayout(new BoxLayout(squirrelHomeSelector, BoxLayout.Y_AXIS));

        final JLabel squirrelConfigLabel = new JLabel("Squirrel sql home folder: ");
        final JLabel squirrelConfigValue = new JLabel("Ongeldige Squirrel home");
        squirrelHomeSelector.add(squirrelConfigLabel);
        squirrelHomeSelector.add(squirrelConfigValue);

        final Button btnOpenDirectoryChooser = new Button();
        btnOpenDirectoryChooser.setLabel("Selecteer Squirrel config map");
        btnOpenDirectoryChooser.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser directoryChooser = new JFileChooser();
                        File homeDir = new File(squirrelConfigHome, HOME_PATH);
                        directoryChooser.setCurrentDirectory(homeDir);
                        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int returnVal = directoryChooser.showDialog(window, null);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File selectedDirectory = directoryChooser.getSelectedFile();
                            if (directoryChooser.getSelectedFile() != null) {
                                aliasFile = new File(selectedDirectory, ALIAS_FILENAME);
                                aliasTreeFile = new File(selectedDirectory, ALIAS_TREE_FILENAME);
                                File driverFile = new File(selectedDirectory, DRIVER_FILENAME);
                                

                                if (!aliasFile.isFile()) {
                                    JOptionPane.showMessageDialog(window,
                                            "AliasFile niet gevonden op: " + aliasFile.getPath(), "Pad onjuist",
                                            JOptionPane.ERROR_MESSAGE);
                                } else if (!aliasTreeFile.isFile()) {
                                    JOptionPane.showMessageDialog(window,
                                            "AliasTreeFile niet gevonden op: " + aliasTreeFile.getPath(),
                                            "Pad onjuist",
                                            JOptionPane.ERROR_MESSAGE);
                                } else if (!driverFile.isFile()) {
                                    JOptionPane.showMessageDialog(window,
                                            "DriverFile niet gevonden op: " + driverFile.getPath(), "Pad onjuist",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    aliasDocument = DocumentUtil.parseDocument(aliasFile);
                                    aliasTreeDocument = DocumentUtil.parseDocument(aliasTreeFile);
                                    driverDocument = DocumentUtil.parseDocument(driverFile);

                                    squirrelConfigValue.setText(selectedDirectory.getAbsolutePath());
                                    squirrelHomeSelector.remove(btnOpenDirectoryChooser);
                                    squirrelConfigHome = selectedDirectory;

                                    final JPanel aanmakenAliasBox = new JPanel();
                                    aanmakenAliasBox.setLayout(new BoxLayout(aanmakenAliasBox, BoxLayout.X_AXIS));
                                    addDriverListView(driverDocument, aanmakenAliasBox);
                                    addAliasConfigFileSelector(window, aanmakenAliasBox);
                                    jPanel.add(aanmakenAliasBox);
                                    jPanel.revalidate();
                                    jPanel.repaint();
                                }
                            }
                        }
                    }

              }

        );

        squirrelHomeSelector.add(btnOpenDirectoryChooser);
        jPanel.add(squirrelHomeSelector);
    }

    public static void addDriverListView(Document driverDocument, final JPanel jPanel) {
        final JPanel driverList = new JPanel();
        driverList.setLayout(new BoxLayout(driverList, BoxLayout.Y_AXIS));

        final JLabel driverListLabel = new JLabel("Drivers:");
        final JList driverListView = new JList(DriverUtil.getDriverNames(driverDocument).toArray());
        driverListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driverListView.setLayoutOrientation(JList.VERTICAL);
        JScrollPane listScroller = new JScrollPane(driverListView);

        driverListView.addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {

                    if (driverListView.getSelectedIndex() != -1) {
                        LOGGER.info("Driver gekozen: {}", driverListView.getSelectedValue());
                        selectedDriver = (String) driverListView.getSelectedValue();
                    }
                }
            }
        });
        driverList.add(driverListLabel);
        driverList.add(listScroller);
        jPanel.add(driverList);
    }

    public static void addAliasConfigFileSelector(final Window window, JPanel jPanel) {
        final JPanel configFileSelect = new JPanel();
        configFileSelect.setLayout(new BoxLayout(configFileSelect, BoxLayout.Y_AXIS));

        final JLabel jsonFileLabel = new JLabel("Alias config Json bestand: ");
        final JLabel jsonFileValue = new JLabel("Ongeldig bestand");
        configFileSelect.add(jsonFileLabel);
        configFileSelect.add(jsonFileValue);

        Button btnOpenFileChooser = new Button();
        btnOpenFileChooser.setLabel("Selecteer Alias config Json bestand");

        btnOpenFileChooser.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        int returnVal = fileChooser.showDialog(window, null);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fileChooser.getSelectedFile();
                            if (selectedFile != null) {

                                jsonFileValue.setText(selectedFile.getAbsolutePath());
                                selectedJsonFile = selectedFile;
                            }
                        }

                    }
                });

        configFileSelect.add(btnOpenFileChooser);
        jPanel.add(configFileSelect);
    }

}
