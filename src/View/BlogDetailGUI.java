package View;

import Model.*;
import dbhelper.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class BlogDetailGUI extends JFrame {
    private JPanel wrapper;
    private JPanel pnl_top;
    private JPanel pnl_bottom;
    private JTextArea txt_blog_content;
    private JTextField fld_comment;
    private JButton btn_add_comment;
    private JTable tbl_comments;
    private JLabel lbl_title;
    private JLabel lbl_author;
    private JLabel lbl_date;
    private JButton btn_like;
    private JButton btn_delete;
    private DefaultTableModel mdl_comments;
    private Object[] row_comments;
    private Blog blog;
    private Comment comment;
    private User user;
    private String userType;
    private final int blogId;

    public BlogDetailGUI(int blogId, Doctor doctor) {
        this.blogId = blogId;
        this.user = doctor;
        this.userType = "DOCTOR";
        initGUI();
    }

    public BlogDetailGUI(int blogId, Hasta hasta) {
        this.blogId = blogId;
        this.user = hasta;
        this.userType = "HASTA";
        initGUI();
    }

    private void initGUI() {
        this.blog = new Blog();
        this.comment = new Comment();

        // Panel oluşturma
        wrapper = new JPanel();
        pnl_top = new JPanel();
        pnl_bottom = new JPanel();
        
        // Bileşenleri oluşturma
        txt_blog_content = new JTextArea();
        txt_blog_content.setEditable(false);
        fld_comment = new JTextField();
        btn_add_comment = new JButton("Yorum Ekle");
        tbl_comments = new JTable();
        lbl_title = new JLabel();
        lbl_author = new JLabel();
        lbl_date = new JLabel();
        btn_like = new JButton("Beğen (0)");
        btn_delete = new JButton("Blogu Sil");
        
        // Panel yerleşimleri
        wrapper.setLayout(new BorderLayout());
        pnl_top.setLayout(new BorderLayout());
        pnl_bottom.setLayout(new BorderLayout());
        
        // Üst panel bileşenleri
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(lbl_title);
        titlePanel.add(lbl_author);
        titlePanel.add(lbl_date);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btn_like);
        buttonPanel.add(btn_delete);
        
        pnl_top.add(titlePanel, BorderLayout.CENTER);
        pnl_top.add(buttonPanel, BorderLayout.EAST);
        pnl_top.add(new JScrollPane(txt_blog_content), BorderLayout.SOUTH);
        
        // Alt panel bileşenleri
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.add(new JScrollPane(tbl_comments), BorderLayout.CENTER);
        
        JPanel addCommentPanel = new JPanel(new BorderLayout());
        addCommentPanel.add(fld_comment, BorderLayout.CENTER);
        addCommentPanel.add(btn_add_comment, BorderLayout.EAST);
        
        pnl_bottom.add(commentPanel, BorderLayout.CENTER);
        pnl_bottom.add(addCommentPanel, BorderLayout.SOUTH);
        
        wrapper.add(pnl_top, BorderLayout.NORTH);
        wrapper.add(pnl_bottom, BorderLayout.CENTER);
        
        add(wrapper);
        setSize(800, 600);
        setLocation(Helper.screenCenterPoint("x", getSize()), Helper.screenCenterPoint("y", getSize()));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Blog Detayı");
        setResizable(false);

        // Yorum tablosu için model
        mdl_comments = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Object[] col_comments = {"ID", "Kullanıcı", "Yorum", "Tarih"};
        mdl_comments.setColumnIdentifiers(col_comments);
        row_comments = new Object[col_comments.length];
        
        tbl_comments.setModel(mdl_comments);
        tbl_comments.getTableHeader().setReorderingAllowed(false);

        // Blog detaylarını yükle
        loadBlogDetails();
        loadComments();

        // Blog silme butonu (sadece doktorlar için ve kendi bloglarını silebilirler)
        btn_delete.setVisible(false); // Varsayılan olarak gizli
        if (userType.equals("DOCTOR")) {
            try {
                ArrayList<Blog> blogs = blog.getBlogList();
                for (Blog b : blogs) {
                    if (b.getId() == blogId && b.getAuthor().equals(user.getName())) {
                        btn_delete.setVisible(true);
                        break;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        btn_delete.addActionListener(e -> {
            if (Helper.confirm("sure")) {
                try {
                    if (blog.deleteBlog(blogId)) {
                        Helper.showMsg("success");
                        dispose();
                    } else {
                        Helper.showMsg("error");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Yorum ekleme butonu
        btn_add_comment.addActionListener(e -> {
            if (Helper.isFieldEmpty(fld_comment)) {
                Helper.showMsg("fill");
            } else {
                try {
                    boolean result = comment.addComment(blogId, user.getId(), fld_comment.getText());
                    if (result) {
                        Helper.showMsg("success");
                        loadComments();
                        fld_comment.setText(null);
                    } else {
                        Helper.showMsg("error");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Beğeni butonu
        btn_like.addActionListener(e -> {
            try {
                boolean hasLiked = blog.likeBlog(blogId, user.getId());
                if (hasLiked) {
                    Helper.showMsg("success");
                    loadBlogDetails();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    private void loadBlogDetails() {
        try {
            ArrayList<Blog> blogs = blog.getBlogList();
            for (Blog b : blogs) {
                if (b.getId() == blogId) {
                    lbl_title.setText(b.getTitle());
                    lbl_author.setText("Yazar: " + b.getAuthor());
                    lbl_date.setText("Tarih: " + b.getDate());
                    txt_blog_content.setText(b.getContent());
                    txt_blog_content.setEditable(false);
                    btn_like.setText("Beğen (" + b.getLikeCount() + ")");
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadComments() {
        DefaultTableModel clearModel = (DefaultTableModel) tbl_comments.getModel();
        clearModel.setRowCount(0);
        try {
            ArrayList<Comment> comments = comment.getCommentsByBlogId(blogId);
            for (Comment c : comments) {
                row_comments[0] = c.getId();
                row_comments[1] = c.getUserName();
                row_comments[2] = c.getContent();
                row_comments[3] = c.getDate();
                mdl_comments.addRow(row_comments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        // GUI bileşenlerinin özel oluşturma işlemleri
    }
} 