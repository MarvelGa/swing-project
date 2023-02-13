package org.example.view;

import org.example.entity.Contact;
import org.example.entity.Employee;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class OfficeEmployee {
    private CustomPanel paintingPanel;
    private JFrame employeeFrame;
    private JPanel panelMain;
    private JLabel labelText;
    private JTextField txtName;
    private JButton btnClick;
    private JTable tblData;

    private boolean isTriangleHidden=false;
    private static final int COLUMN_QUANTITY_FOR_NAME_FIELD = 1;
    private static final String NAME_OF_FIRST_COLUMN = "Employee's Name";
    private static final String NAME_OF_NEXT_COLUMN = "Phone number";
    private static final int MAIN_WIDTH = 1100;
    private static final int MAIN_HEIGHT = 700;
    private static final int TABLE_WIDTH = 800;
    private static final int TABLE_HEIGHT = 200;

    public OfficeEmployee() {
        saveEmployeeInDataBase();
    }

    public void createView() {
        employeeFrame = new JFrame("Employee View");
        employeeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        employeeFrame.setSize(MAIN_WIDTH, MAIN_HEIGHT);

        paintingPanel = new CustomPanel();

        panelMain = new JPanel();
        txtName = new JTextField(10);
        labelText = new JLabel("Enter name");
        btnClick = new JButton("Find");



        panelMain.add(labelText);
        panelMain.add(txtName);
        panelMain.add(btnClick);
        employeeFrame.setVisible(true);
        renderTable();

        btnClick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredName = txtName.getText();
                List<Employee> employees = getAllEmployeeFromDataBase();

                if (enteredName.equals("")) {
                    return;
                }
                List<String> numbersList = employees.stream()
                        .filter(em -> em.getName().equals(enteredName.toLowerCase().substring(0, 1).toUpperCase() + txtName.getText().substring(1)))
                        .map(em -> em.getContacts())
                        .flatMap(con -> con.stream())
                        .map(Contact::getPhoneNumber)
                        .collect(Collectors.toList());

                if (numbersList == null) {
                    JOptionPane.showMessageDialog(btnClick,
                            String.format("Employee with name" + '"' + "%s" + '"' + " is not found", txtName.getText()));
                }
                if (numbersList.size() == 0) {
                    JOptionPane.showMessageDialog(btnClick,
                            String.format("Employee with name" + '"' + "%s" + '"' + " has not had contacts yet", txtName.getText()));
                }
                if (numbersList.size() > 0) {
                    String employeePhoneNumbers = numbersList.stream()
                            .collect(Collectors.joining(", "));
                    JOptionPane.showMessageDialog(btnClick,
                            String.format("Employee with name" + '"' + "%s" + '"' + " is found, his contacts: %s", txtName.getText(), employeePhoneNumbers));
                    paintingPanel.setVisible(false);
                    isTriangleHidden =true;
                    renderTable();
                }
                txtName.setText("");
            }
        });
    }

    private void renderTable() {
        List<Employee> bookList = getAllEmployeeFromDataBase();

        Map<String, List<String>> nameToPhone = new LinkedHashMap();
        for (Employee el : bookList) {
            nameToPhone.put(el.getName(), bookList.stream()
                    .filter(el1 -> el1.getName().equals(el.getName()))
                    .flatMap(list -> list.getContacts().stream())
                    .map(Contact::getPhoneNumber)
                    .collect(Collectors.toList()));

        }
        Integer maxQuantityOfPhoneNumbers = getMaxContactQuantity(nameToPhone);
        int numberOfColumns = maxQuantityOfPhoneNumbers + COLUMN_QUANTITY_FOR_NAME_FIELD;
        String[] columnNames = new String[numberOfColumns];
        tblData = new JTable(0, numberOfColumns);
        JScrollPane tableSP = new JScrollPane(tblData);
        tableSP.setPreferredSize(new Dimension(TABLE_WIDTH, TABLE_HEIGHT));
        employeeFrame.setLocationRelativeTo(null);
        if (!isTriangleHidden){
            paintingPanel.add(panelMain);
            paintingPanel.add(tableSP, BorderLayout.CENTER);
            employeeFrame.add(paintingPanel);
        }else{
            employeeFrame.add(tableSP,  BorderLayout.CENTER);
            employeeFrame.add(panelMain, BorderLayout.WEST);
        }
        DefaultTableModel model = (DefaultTableModel) tblData.getModel();
        setColumnTitle(model, columnNames);
        fillTable(nameToPhone, model);
    }

    private void saveEmployeeInDataBase() {
        SessionFactory sessionFactory = null;
        Session session = null;
        Transaction tx = null;
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            Map<String, List<String>> nameToPhoneNumber = new LinkedHashMap<>();
            nameToPhoneNumber.put("Sam", List.of("+3806612789", "+3809912787", "+3806712781"));
            nameToPhoneNumber.put("Nick", List.of("+3804412789"));
            nameToPhoneNumber.put("Sofia", List.of("+3809812789", "+3805612788"));
            nameToPhoneNumber.put("John", List.of("+3804512722"));
            nameToPhoneNumber.put("Stefan", List.of("+3805521165", "+38016555457"));
            nameToPhoneNumber.put("Ivan", List.of("+38028465525", "+38056655527"));
            nameToPhoneNumber.put("Petro", List.of("+3809555224", "+3805588822"));
            nameToPhoneNumber.put("Vasil", List.of("+38082582222", "+3805666478898"));
            nameToPhoneNumber.put("Alina", List.of("+380845664453", "+38064788419"));

            for (Map.Entry<String, List<String>> elem : nameToPhoneNumber.entrySet()) {
                List<Contact> contacts = new LinkedList<>();
                String name = elem.getKey();
                Employee employee = new Employee(name);
                session.save(employee);
                for (String phoneNumbers : elem.getValue()) {
                    Contact contact = new Contact(phoneNumbers, employee);
                    session.save(contact);
                    contacts.add(contact);
                }
                employee.setContacts(contacts);
                session.save(employee);
            }
            tx.commit();
        } catch (Exception e) {
            System.out.println("Exception occured. " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!sessionFactory.isClosed()) {
                System.out.println("Closing SessionFactory");
                sessionFactory.close();
            }
        }
    }

    private List<Employee> getAllEmployeeFromDataBase() {
        SessionFactory sessionFactory = null;
        Session session = null;
        Transaction tx = null;
        List<Employee> bookList = new LinkedList<>();
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            Query qry = session.createQuery("from Employee e");
            bookList = qry.list();
            tx.commit();
        } catch (Exception e) {
            System.out.println("Exception occured. " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!sessionFactory.isClosed()) {
                System.out.println("Closing SessionFactory");
                sessionFactory.close();
            }
        }
        return bookList;
    }

    private Integer getMaxContactQuantity(Map<String, List<String>> nameToPhoneNumber) {
        return nameToPhoneNumber.values().stream().filter(el -> el != null).mapToInt(el -> el.size()).max().orElse(0);
    }

    private void setColumnTitle(DefaultTableModel model, String[] columnNames) {
        columnNames[0] = NAME_OF_FIRST_COLUMN;
        for (int i = 1; i < columnNames.length; i++) {
            columnNames[i] = NAME_OF_NEXT_COLUMN;
            tblData.getColumnModel().getColumn(i).setHeaderValue(NAME_OF_NEXT_COLUMN);
        }
        model.setColumnIdentifiers(columnNames);
    }

    private void fillTable(Map<String, List<String>> nameToPhoneNumber, DefaultTableModel model) {
        for (Map.Entry<String, List<String>> el : nameToPhoneNumber.entrySet()) {
            String[] row = new String[el.getValue().size() + 1];
            row[0] = el.getKey();
            for (int i = 1, j = 0; i < row.length; i++, j++) {
                row[i] = el.getValue().get(j);
            }
            model.addRow(row);
        }
    }
}

class CustomPanel extends JPanel {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 700;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Path2D path = new Path2D.Double();
        path.moveTo(0, HEIGHT);
        path.lineTo(WIDTH / 2, 0);
        path.lineTo(WIDTH, HEIGHT);
        path.closePath();
        g2.setStroke(new BasicStroke(0));
        g2.setColor(Color.MAGENTA);
        g2.draw(path);
    }
}
