import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextEditor extends JFrame {
    public final String LOCAL_PATH = Paths.get("").toAbsolutePath().toString();
    private static File currentFile;
    JPanel panelNorth;
    JTextArea textArea;
    JTextField searchField;
    JButton saveButton;
    JButton loadButton;
    JButton searchMatchButton;
    JButton prevMatchButton;
    JButton nextMatchButton;
    JCheckBox useRegexCheckBox;
    JFileChooser fileChooser;
    Search search;
    private static boolean useRegex = false;

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 300);
        setLocationRelativeTo(null);

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        setTitle("Java Text Editor");
        panelNorth = new JPanel();
        add(panelNorth, BorderLayout.NORTH);

        fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        fileChooser.setVisible(false);
        add(fileChooser);

        initMainText();

        initFilePane();

        initMenu();
    }

    private void initMainText() {
        textArea = new JTextArea();
        textArea.setName("TextArea");
        JScrollPane textAreaScroll = new JScrollPane(textArea);
        textAreaScroll.setName("ScrollPane");
        textAreaScroll.setBounds(10, 10, 250, 200);
        add(textAreaScroll, BorderLayout.CENTER);
    }

    private void initFilePane() {
        searchField = new JTextField();
        DimensionUIResource dimension = new DimensionUIResource(120, 30);
        searchField.setName("SearchField");
        searchField.setMinimumSize(dimension);
        searchField.setPreferredSize(dimension);
        searchField.setBounds(10, 10, 120, 30);

        initButtons();
        initCheckBox();

        panelNorth.add(loadButton);
        panelNorth.add(saveButton);
        panelNorth.add(searchField);
        panelNorth.add(searchMatchButton);
        panelNorth.add(prevMatchButton);
        panelNorth.add(nextMatchButton);
        panelNorth.add(useRegexCheckBox);
    }

    private void initCheckBox() {
        useRegexCheckBox = new JCheckBox("Use regex");
        useRegexCheckBox.setName("UseRegExCheckbox");

        useRegexCheckBox.addItemListener(actionEvent -> {
            if (actionEvent.getStateChange() == 1) {
                setRegexSearch();
            }
            else {
                setNormalSearch();
            }
            if (search != null) {
                search();
            }
        });
    }

    private void setRegexSearch() {
        useRegex = true;
    }

    private void setNormalSearch() {
        useRegex = false;
    }

    private void initButtons() {
        saveButton = new JButton(new ImageIcon("src/save.png"));
        saveButton.setName("SaveButton");
        // saveButton.add("saveLabel", new JLabel("Save")); // uncomment for text based label for button
        saveButton.addActionListener(actionEvent -> {
            try {
                saveTextFile(textArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        loadButton = new JButton(new ImageIcon("src/folder.png"));
        loadButton.setName("OpenButton");
        // loadButton.add("loadLabel", new JLabel("Load")); // uncomment for text based label button
        loadButton.addActionListener(actionEvent -> {
            // loadTextFile(fileNameField.getText());
            openFile();
        });

        searchMatchButton = new JButton(new ImageIcon("src/search.png"));
        searchMatchButton.setName("StartSearchButton");
        searchMatchButton.addActionListener(actionEvent -> {
            search();
        });

        prevMatchButton = new JButton(new ImageIcon("src/back.png"));

        prevMatchButton.setName("PreviousMatchButton");
        prevMatchButton.addActionListener(actionEvent -> {
            prevMatch();
        });

        nextMatchButton = new JButton(new ImageIcon("src/next.png"));
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.addActionListener(actionEvent -> {
            nextMatch();
        });
    }

    private void openFile() {
        fileChooser.setVisible(true);
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadTextFile(selectedFile);
        }
        fileChooser.setVisible(false);
    }

    // DEPRECIATED
    private void loadTextFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();

        Path path = Path.of(LOCAL_PATH + "//" + fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(path.toUri()));
            byte[] bytes = fileInputStream.readAllBytes();
            textArea.setText(new String(bytes));
        } catch (FileNotFoundException e) {
            textArea.setText("");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTextFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();

        Path path = Paths.get(file.getPath());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(path.toUri()));
            byte[] bytes = fileInputStream.readAllBytes();
            textArea.setText(new String(bytes));
        } catch (FileNotFoundException e) {
            textArea.setText("");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFile = file;
    }

    private void saveTextFile(String data) throws IOException {
        fileChooser.setVisible(true);
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File fileN = fileChooser.getSelectedFile();

            FileWriter fileWriter = new FileWriter(fileN, false);
            fileWriter.write(data);
            fileWriter.close();
        }
    }

    // DEPRECIATED
    private void saveTextFile(String fileName, String data) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, false);
        fileWriter.write(data);
        fileWriter.close();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        initFileMenu(menuBar);
        initSearchMenu(menuBar);

        setJMenuBar(menuBar);
    }

    private void initSearchMenu(JMenuBar menuBar) {
        // start search, previous search, next match, use regular expressions
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");

        JMenuItem itemStartSearch = new JMenuItem("Start Search");
        itemStartSearch.setName("MenuStartSearch");
        itemStartSearch.addActionListener(actionEvent -> {
            search();
        });

        JMenuItem itemNextSearch = new JMenuItem("Next Search");
        itemNextSearch.setName("MenuNextMatch");
        itemNextSearch.addActionListener(actionEvent -> {
            nextMatch();
        });

        JMenuItem itemPrevSearch = new JMenuItem("Previous Search");
        itemPrevSearch.setName("MenuPreviousMatch");
        itemPrevSearch.addActionListener(actionEvent -> {
            prevMatch();
        });

        JMenuItem itemUseRegExp = new JMenuItem("Use Regular Expression");
        itemUseRegExp.setName("MenuUseRegExp");
        itemUseRegExp.addActionListener(actionEvent -> {
            useRegexCheckBox.setSelected(!useRegex);
        });
        searchMenu.add(searchMenu);
        searchMenu.add(itemStartSearch);
        searchMenu.add(itemNextSearch);
        searchMenu.add(itemPrevSearch);
        searchMenu.add(itemUseRegExp);
        menuBar.add(searchMenu);
    }

    private void initFileMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem menuLoad = new JMenuItem("Load");
        menuLoad.setName("MenuOpen");
        menuLoad.addActionListener(actionEvent -> {
            openFile();
        });

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.addActionListener(actionEvent -> {
            try {
                saveTextFile(textArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.addActionListener(actionEvent -> exitTextEditor());

        fileMenu.add(menuLoad);
        fileMenu.add(menuSave);
        fileMenu.addSeparator();
        fileMenu.add(menuExit);
        menuBar.add(fileMenu);
    }

    private void exitTextEditor() {
        this.dispose();
        System.exit(0);
    }

    private void search() {
        search = new Search(searchField.getText(), textArea.getText(), useRegex);
        nextMatch();
    }

    private void highlightMatch(List<Integer> result) {
        textArea.setCaretPosition(result.get(0) + (result.get(1) - result.get(0)));
        textArea.select(result.get(0), result.get(1));
        textArea.grabFocus();
    }

    private void nextMatch() {
        if (search != null) {
            highlightMatch(search.nextMatch());
        }
    }

    private void prevMatch() {
        if (search != null) {
            highlightMatch(search.previousMatch());
        }
    }

    static class Search {
        private final String search;
        private final int size;
        private final Matcher matcher;
        private final Deque<List<Integer>> results = new ArrayDeque<>();
        private final Deque<List<Integer>> current = new ArrayDeque<>();

        public Search(String search, String data, boolean isRegex) {
            this.search = search;
            this.size = data.length();

            Pattern pattern = Pattern.compile(search);
            matcher = pattern.matcher(data);
            System.out.println(matcher.matches());
            while (matcher.find()) {
                List<Integer> startEnd = new ArrayList<Integer>();
                startEnd.add(matcher.start());
                startEnd.add(matcher.end());
                results.add(startEnd);
                if (isWholeString(matcher.start(), matcher.end())) {
                    break;
                }
            }
        }

        private boolean isWholeString(int start, int end) {
            System.out.println(start + "  " + end);
            System.out.println(this.size);
            if (start == 0 && end == this.size) {
                return true;
            }
            return false;
        }

        public List<Integer> nextMatch() {
            List<Integer> headMatch = results.poll();
            current.add(headMatch);
            if (current.size() > 1) {
                results.addLast(current.pollFirst());
            }
            System.out.println("start: " + headMatch.get(0) + " end: " + headMatch.get(1));
            return headMatch;
        }

        public List<Integer> previousMatch() {
            List<Integer> tailMatch = results.pollLast();
            current.add(tailMatch);
            if (current.size() > 1) {
                results.addFirst(current.poll());
            }
            System.out.println("start: " + tailMatch.get(0) + " end: " + tailMatch.get(1));
            return tailMatch;
        }
    }

    public static void main(String args[]) {
        TextEditor textEditor = new TextEditor();
    }
}
