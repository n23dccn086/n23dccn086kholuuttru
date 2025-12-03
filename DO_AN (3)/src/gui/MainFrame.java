package gui;

import manager.EmployeeManager;
import manager.DepartmentManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JTable empTable, deptTable;
    private JTextField searchEmpTF, searchDeptTF;

    private final EmployeeManager empMgr = new EmployeeManager();
    private final DepartmentManager deptMgr = new DepartmentManager();

    public MainFrame() {
        setTitle("Quản Lý Nhân Viên - OOP");
        setSize(1250, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Nhân Viên", createEmployeePanel());
        tabbedPane.addTab("Phòng Ban", createDepartmentPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ======================= TAB NHÂN VIÊN =======================
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"ID", "Họ tên", "Tuổi", "Giới tính", "Phòng ban", "Chức vụ", "Lương", "SĐT", "Loại", "Bonus"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        empTable = new JTable(model);
        panel.add(new JScrollPane(empTable), BorderLayout.CENTER);

        // Thanh tìm kiếm + nút
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tìm kiếm:"));
        searchEmpTF = new JTextField(25);
        top.add(searchEmpTF);

        JButton searchBtn = new JButton("Tìm");
        JButton addBtn    = new JButton("Thêm");
        JButton editBtn   = new JButton("Sửa");
        JButton delBtn    = new JButton("Xóa");
        JButton refreshBtn = new JButton("Làm mới");
        JButton statsBtn   = new JButton("Thống kê");
        JButton importBtn  = new JButton("Import");

        // ComboBox sắp xếp
        String[] sortOptions = {"Sắp xếp theo...", "ID", "Tên", "Lương", "Tuổi"};
        JComboBox<String> sortComboBox = new JComboBox<>(sortOptions);
        JButton sortBtn = new JButton("Sắp xếp");

        top.add(searchBtn); top.add(addBtn); top.add(editBtn); top.add(delBtn);
        top.add(refreshBtn); top.add(statsBtn); top.add(importBtn);
        top.add(sortComboBox); top.add(sortBtn); // THÊM COMBOBOX VÀ NÚT SẮP XẾP
        panel.add(top, BorderLayout.NORTH);

        // Sự kiện nút
        searchBtn.addActionListener(e -> searchEmployee(model));
        addBtn.addActionListener(e -> addEmployee(model));
        editBtn.addActionListener(e -> editEmployee(model));
        delBtn.addActionListener(e -> deleteEmployee(model));
        refreshBtn.addActionListener(e -> loadEmployeeTable(model));
        statsBtn.addActionListener(e -> empMgr.statistics());
        importBtn.addActionListener(e -> importEmployeeData(model));
        sortBtn.addActionListener(e -> {
            String selectedSort = (String) sortComboBox.getSelectedItem();
            sortEmployees(selectedSort, model);
        });

        loadEmployeeTable(model);
        return panel;
    }

    // Phương thức sắp xếp tổng quát
    private void sortEmployees(String sortType, DefaultTableModel model) {
        if (sortType == null || sortType.equals("Sắp xếp theo...")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tiêu chí sắp xếp!");
            return;
        }

        List<Employee> sortedList;
        switch (sortType) {
            case "ID":
                sortedList = empMgr.sortByID();
                break;
            case "Tên":
                sortedList = empMgr.sortByName();
                break;
            case "Lương":
                sortedList = empMgr.sortBySalary();
                break;
            case "Tuổi":
                sortedList = empMgr.sortByAge();
                break;
            default:
                sortedList = empMgr.getAll();
                break;
        }

        loadSortedEmployeeTable(model, sortedList);
        JOptionPane.showMessageDialog(this, "Đã sắp xếp nhân viên theo " + sortType + "!");
    }

    // ======================= TAB PHÒNG BAN =======================
    private JPanel createDepartmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Mã PB", "Tên phòng ban", "Mô tả", "SL tối đa"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        deptTable = new JTable(model);
        panel.add(new JScrollPane(deptTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tìm kiếm:"));
        searchDeptTF = new JTextField(25);
        top.add(searchDeptTF);

        JButton searchBtn = new JButton("Tìm");
        JButton addBtn    = new JButton("Thêm");
        JButton editBtn   = new JButton("Sửa");
        JButton delBtn    = new JButton("Xóa");
        JButton refreshBtn = new JButton("Làm mới");
        JButton importBtn  = new JButton("Import"); 
        JButton statsBtn = new JButton("Thống kê");

        top.add(searchBtn); top.add(addBtn); top.add(editBtn); top.add(delBtn);
        top.add(refreshBtn); top.add(importBtn);  top.add(statsBtn);
        panel.add(top, BorderLayout.NORTH);

        searchBtn.addActionListener(e -> searchDepartment(model));
        addBtn.addActionListener(e -> addDepartment(model));
        editBtn.addActionListener(e -> editDepartment(model));
        delBtn.addActionListener(e -> deleteDepartment(model));
        refreshBtn.addActionListener(e -> loadDepartmentTable(model));
        importBtn.addActionListener(e -> importDepartmentData(model));
        statsBtn.addActionListener(e -> deptMgr.statistics());
        loadDepartmentTable(model);
        return panel;
    }

    // ======================= CÁC HÀM NHÂN VIÊN =======================
    private void loadEmployeeTable(DefaultTableModel m) {
        m.setRowCount(0);
        List<Employee> list = empMgr.getAll();
        for (Employee e : list) {
            m.addRow(new Object[]{
                    e.getEmployeeID(),
                    e.getName(),
                    e.getAge(),
                    e.getGender(),
                    e.getDepartmentID(),
                    e.getPosition(),
                    String.format("%,.0f", e.getSalary()),
                    e.getPhone(),
                    e instanceof FullTimeEmployee ? "Full-time" : "Part-time",
                    String.format("%,.0f", e.calculateBonus())
            });
        }
    }

    private void searchEmployee(DefaultTableModel m) {
        String kw = searchEmpTF.getText().trim().toLowerCase();
        m.setRowCount(0);
        List<Employee> all = empMgr.getAll();
        for (Employee e : all) {
            if (e.getEmployeeID().toLowerCase().contains(kw) ||
                    e.getName().toLowerCase().contains(kw) ||
                    e.getDepartmentID().toLowerCase().contains(kw)) {
                m.addRow(new Object[]{
                        e.getEmployeeID(), e.getName(), e.getAge(), e.getGender(),
                        e.getDepartmentID(), e.getPosition(),
                        String.format("%,.0f", e.getSalary()), e.getPhone(),
                        e instanceof FullTimeEmployee ? "Full-time" : "Part-time",
                        String.format("%,.0f", e.calculateBonus())
                });
            }
        }
    }

    private void addEmployee(DefaultTableModel m) {
        showEmployeeDialog("Thêm Nhân Viên", null, m);
    }

    private void editEmployee(DefaultTableModel m) {
        int row = empTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
            return;
        }
        String id = (String) m.getValueAt(row, 0);
        Employee emp = empMgr.search(id);
        if (emp != null) showEmployeeDialog("Sửa Nhân Viên", emp, m);
    }

    private void deleteEmployee(DefaultTableModel m) {
        int row = empTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!");
            return;
        }
        String id = (String) m.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa nhân viên ID = " + id + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            empMgr.remove(id);
            loadEmployeeTable(m);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
        }
    }

    // ======================= CÁC HÀM PHÒNG BAN =======================
    private void loadDepartmentTable(DefaultTableModel m) {
        m.setRowCount(0);
        List<Department> list = deptMgr.getAll();
        for (Department d : list) {
            m.addRow(new Object[]{d.getDepartmentID(), d.getName(), d.getDescription(), d.getMaxEmployees()});
        }
    }

    private void searchDepartment(DefaultTableModel m) {
        String kw = searchDeptTF.getText().trim().toLowerCase();
        m.setRowCount(0);
        List<Department> all = deptMgr.getAll();
        for (Department d : all) {
            if (d.getDepartmentID().toLowerCase().contains(kw) || d.getName().toLowerCase().contains(kw)) {
                m.addRow(new Object[]{d.getDepartmentID(), d.getName(), d.getDescription(), d.getMaxEmployees()});
            }
        }
    }

    private void addDepartment(DefaultTableModel m) {
        showDepartmentDialog("Thêm Phòng Ban", null, m);
    }

    private void editDepartment(DefaultTableModel m) {
        int row = deptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban cần sửa!");
            return;
        }
        String id = (String) m.getValueAt(row, 0);
        Department dept = deptMgr.search(id);
        if (dept != null) showDepartmentDialog("Sửa Phòng Ban", dept, m);
    }

    private void deleteDepartment(DefaultTableModel m) {
        int row = deptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng ban cần xóa!");
            return;
        }
        String id = (String) m.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa phòng ban ID = " + id + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            deptMgr.remove(id);
            loadDepartmentTable(m);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
        }
    }

    // ======================= DIALOG THÊM/SỬA NHÂN VIÊN =======================
    // ======================= DIALOG THÊM/SỬA NHÂN VIÊN =======================
    private void showEmployeeDialog(String title, Employee emp, DefaultTableModel model) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(450, 600);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField[] fields = new JTextField[9];
        String[] labels = {"ID", "Họ tên", "Tuổi", "Giới tính", "Phòng ban ID", "Chức vụ", "Lương", "SĐT", "Loại (full/part)"};

        for (int i = 0; i < labels.length; i++) {
            d.add(new JLabel(labels[i] + ":"));
            fields[i] = new JTextField(20);
            d.add(fields[i]);
        }

        if (emp != null) {
            fields[0].setText(emp.getEmployeeID());
            fields[1].setText(emp.getName());
            fields[2].setText(String.valueOf(emp.getAge()));
            fields[3].setText(emp.getGender());
            fields[4].setText(emp.getDepartmentID());
            fields[5].setText(emp.getPosition());
            fields[6].setText(String.valueOf(emp.getSalary()));
            fields[7].setText(emp.getPhone());
            fields[8].setText(emp instanceof FullTimeEmployee ? "full" : "part");
            fields[0].setEditable(false);
        }

        JButton save = new JButton(emp == null ? "Thêm mới" : "Cập nhật");
        save.addActionListener(e -> {
            try {
                String id = fields[0].getText().trim();
                String name = fields[1].getText().trim();
                String type = fields[8].getText().trim().toLowerCase();

                // KIỂM TRA TRÙNG ID KHI THÊM MỚI
                if (emp == null) {
                    if (empMgr.search(id) != null) {
                        JOptionPane.showMessageDialog(d, "ID nhân viên đã tồn tại! Vui lòng chọn ID khác.");
                        return;
                    }
                }

                // Kiểm tra các trường bắt buộc
                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "ID và Họ tên không được để trống!");
                    return;
                }

                // Kiểm tra phòng ban tồn tại
                String deptId = fields[4].getText().trim();
                if (deptMgr.search(deptId) == null) {
                    JOptionPane.showMessageDialog(d, "Phòng ban ID '" + deptId + "' không tồn tại!");
                    return;
                }

                Employee newEmp = type.equals("full") ?
                        new FullTimeEmployee(id, name,
                                Integer.parseInt(fields[2].getText()), fields[3].getText(),
                                deptId, fields[5].getText(),
                                Double.parseDouble(fields[6].getText()), fields[7].getText(), 24) :
                        new PartTimeEmployee(id, name,
                                Integer.parseInt(fields[2].getText()), fields[3].getText(),
                                deptId, fields[5].getText(),
                                Double.parseDouble(fields[6].getText()), fields[7].getText(), 20);

                if (emp == null) {
                    empMgr.add(newEmp);
                    JOptionPane.showMessageDialog(d, "Thêm thành công!");
                } else {
                    empMgr.update(newEmp);
                    JOptionPane.showMessageDialog(d, "Cập nhật thành công!");
                }
                loadEmployeeTable(model);
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Tuổi và Lương phải là số hợp lệ!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Dữ liệu không hợp lệ! Vui lòng kiểm tra lại.\n" + ex.getMessage());
            }
        });

        d.add(new JLabel());
        d.add(save);
        d.setVisible(true);
    }

    // ======================= DIALOG THÊM/SỬA PHÒNG BAN =======================
    private void showDepartmentDialog(String title, Department dept, DefaultTableModel model) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField idF = new JTextField(20);
        JTextField nameF = new JTextField(20);
        JTextField descF = new JTextField(20);
        JTextField maxF = new JTextField(20);

        d.add(new JLabel("Mã phòng ban:")); d.add(idF);
        d.add(new JLabel("Tên phòng ban:")); d.add(nameF);
        d.add(new JLabel("Mô tả:")); d.add(descF);
        d.add(new JLabel("Số lượng tối đa:")); d.add(maxF);

        if (dept != null) {
            idF.setText(dept.getDepartmentID());
            nameF.setText(dept.getName());
            descF.setText(dept.getDescription());
            maxF.setText(String.valueOf(dept.getMaxEmployees()));
            idF.setEditable(false);
        }

        JButton save = new JButton(dept == null ? "Thêm mới" : "Cập nhật");
        save.addActionListener(e -> {
            try {
                Department newDept = new Department(
                        idF.getText().trim(),
                        nameF.getText().trim(),
                        descF.getText().trim(),
                        Integer.parseInt(maxF.getText())
                );

                if (dept == null) {
                    deptMgr.add(newDept);
                    JOptionPane.showMessageDialog(d, "Thêm phòng ban thành công!");
                } else {
                    deptMgr.update(newDept);
                    JOptionPane.showMessageDialog(d, "Cập nhật thành công!");
                }
                loadDepartmentTable(model);
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Dữ liệu không hợp lệ!");
            }
        });

        d.add(new JLabel());
        d.add(save);
        d.setVisible(true);
    }

    // ======================= CHỨC NĂNG IMPORT =======================

    // Import dữ liệu nhân viên từ file
    private void importEmployeeData(DefaultTableModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file txt để import nhân viên");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Import dữ liệu từ: " + filePath + "?",
                    "Xác nhận Import", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                importEmployeesFromFile(filePath, model);
            }
        }
    }

    // Import dữ liệu phòng ban từ file
    private void importDepartmentData(DefaultTableModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file txt để import phòng ban");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Import dữ liệu phòng ban từ: " + filePath + "?",
                    "Xác nhận Import", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                importDepartmentsFromFile(filePath, model);
            }
        }
    }

    // Xử lý import nhân viên từ file
    private void importEmployeesFromFile(String filePath, DefaultTableModel model) {
        int importedCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Bỏ qua header
                if (isFirstLine && (line.contains("ID") || line.contains("Họ tên"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        int age = Integer.parseInt(parts[2].trim());
                        String gender = parts[3].trim();
                        String deptId = parts[4].trim();
                        String position = parts[5].trim();
                        double salary = Double.parseDouble(parts[6].trim());
                        String phone = parts[7].trim();
                        String type = parts[8].trim().toLowerCase();

                        // Kiểm tra trùng ID
                        if (empMgr.search(id) != null) {
                            System.out.println("Bỏ qua nhân viên trùng ID: " + id);
                            continue;
                        }

                        Employee newEmp;
                        if (type.equals("full")) {
                            int annualLeave = parts.length > 9 ? Integer.parseInt(parts[9].trim()) : 24;
                            newEmp = new FullTimeEmployee(id, name, age, gender, deptId, position, salary, phone, annualLeave);
                        } else {
                            int hoursPerWeek = parts.length > 9 ? Integer.parseInt(parts[9].trim()) : 20;
                            newEmp = new PartTimeEmployee(id, name, age, gender, deptId, position, salary, phone, hoursPerWeek);
                        }

                        empMgr.add(newEmp);
                        importedCount++;

                    } catch (Exception ex) {
                        System.out.println("Lỗi xử lý dòng: " + line + " - " + ex.getMessage());
                    }
                }
            }

            loadEmployeeTable(model);
            JOptionPane.showMessageDialog(this,
                    "Import thành công " + importedCount + " nhân viên từ file!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi import file: " + ex.getMessage());
        }
    }

    // Xử lý import phòng ban từ file
    private void importDepartmentsFromFile(String filePath, DefaultTableModel model) {
        int importedCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Bỏ qua header
                if (isFirstLine && (line.contains("Mã PB") || line.contains("Tên phòng ban"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String description = parts[2].trim();
                        int maxEmployees = Integer.parseInt(parts[3].trim());

                        // Kiểm tra trùng ID
                        if (deptMgr.search(id) != null) {
                            System.out.println("Bỏ qua phòng ban trùng ID: " + id);
                            continue;
                        }

                        Department newDept = new Department(id, name, description, maxEmployees);
                        deptMgr.add(newDept);
                        importedCount++;

                    } catch (Exception ex) {
                        System.out.println("Lỗi xử lý dòng: " + line + " - " + ex.getMessage());
                    }
                }
            }

            loadDepartmentTable(model);
            JOptionPane.showMessageDialog(this,
                    "Import thành công " + importedCount + " phòng ban từ file!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi import file: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    // ======================= SẮP XẾP NHÂN VIÊN =======================

    // Sắp xếp nhân viên theo ID
    private void sortEmployeesByID(DefaultTableModel model) {
        List<Employee> sortedList = empMgr.sortByID();
        loadSortedEmployeeTable(model, sortedList);
        JOptionPane.showMessageDialog(this, "Đã sắp xếp nhân viên theo ID!");
    }

    // Hiển thị danh sách nhân viên đã sắp xếp
    private void loadSortedEmployeeTable(DefaultTableModel m, List<Employee> sortedList) {
        m.setRowCount(0);
        for (Employee e : sortedList) {
            m.addRow(new Object[]{
                    e.getEmployeeID(),
                    e.getName(),
                    e.getAge(),
                    e.getGender(),
                    e.getDepartmentID(),
                    e.getPosition(),
                    String.format("%,.0f", e.getSalary()),
                    e.getPhone(),
                    e instanceof FullTimeEmployee ? "Full-time" : "Part-time",
                    String.format("%,.0f", e.calculateBonus())
            });
        }
    }

}
