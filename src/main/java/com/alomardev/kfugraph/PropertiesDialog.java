package com.alomardev.kfugraph;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PropertiesDialog extends JDialog implements ActionListener {

    public static interface PropertiesListener {

        void onPositive(HashMap<String, String> values);
    }

    private final Property[] props;
    private final String positiveText, negativeText;

    private PropertiesListener callback;
    private JButton positiveBtn;
    private JButton negativeBtn;

    public void setPropertiesListener(PropertiesListener callback) {
        this.callback = callback;
    }

    public Property[] getProps() {
        return props;
    }

    public PropertiesDialog(String title, Property... props) {
        this(title, "Done", "Cancel", true, props);
    }

    public PropertiesDialog(String title, String positiveText, String negativeText, boolean modal, Property... props) {
        this.props = props;
        this.positiveText = positiveText;
        this.negativeText = negativeText;
        setModal(modal);
        setTitle(title);
        init();
    }

    private void init() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.insets.left = c.insets.right = 10;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField selectedTextField = null;

        for (Property p : props) {
            JLabel l = new JLabel(p.getLabel());
            JTextField t = new JTextField(25);
            if (selectedTextField == null) selectedTextField = t;

            if (p.getDescription() != null) {
                t.setToolTipText(p.getDescription());
            }

            if (p.getValue() != null) {
                t.setText(p.getValue());
            }

            c.insets.top = 5;
            c.gridy++;
            add(l, c);

            c.insets.top = 3;
            c.gridy++;
            add(t, c);

            p.setTag(t);

            t.addActionListener(this);
        }

        positiveBtn = new JButton(positiveText);
        negativeBtn = new JButton(negativeText);

        c.gridy++;
        c.insets.bottom = 5;
        c.insets.top = 10;
        c.insets.left = c.insets.right = 10;

        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        add(negativeBtn, c);

        c.gridx++;
        c.weightx = 0;
        c.insets.left = 0;
        add(positiveBtn, c);

        pack();
        setResizable(false);

        positiveBtn.addActionListener(this);
        negativeBtn.addActionListener(this);
        
        if (selectedTextField != null) {
            selectedTextField.selectAll();
        }
    }

    public void setLocation(Container holder, MouseEvent ev) {
        Container parent = holder;
        while (!(holder instanceof JFrame)) {
            if (parent.getParent() == null) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent != null) {
            int x = holder.getX() + ev.getX() + parent.getLocation().x - getWidth() / 2;
            int y = holder.getY() + ev.getY() + parent.getLocation().y - getHeight();
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            setLocation(x, y);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean submit = e.getSource() == positiveBtn;
        if (e.getSource() instanceof JTextField) {
            JTextField next = null;
            for (Component c : getComponents()) {
                if (c instanceof JTextField) {
                    if (next == null && c != e.getSource()) {
                        next = (JTextField) e.getSource();
                    }
                }
            }

            if (next != null) {
                next.requestFocus();
            } else {
                submit |= true;
            }
        }
        if (submit && callback != null) {
            HashMap<String, String> values = new HashMap<>();
            for (Property p : props) {
                p.setValue(((JTextField) p.getTag()).getText());

                values.put(p.getKey(), p.getValue());
            }
            callback.onPositive(values);
        }

        dispose();
    }

    public static class Property {

        private Object tag;
        private String label;
        private String desc;
        private String key;
        private String value;

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Property(String key, String label) {
            this(key, label, null);
        }

        public Property(String key, String label, String desc) {
            this.key = key;
            this.label = label;
            this.desc = desc;
        }

    }
}
