package theic2.xenobyteport.gui.swing;

import theic2.xenobyteport.api.gui.ColorPicker;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ColorPickerGui extends XenoJFrame implements ChangeListener {

    protected JPanel sliders;
    private ColorPicker picker;
    private JSlider r, g, b, a;
    private JTextField info;
    private JPanel viewer;

    public ColorPickerGui(String title, ColorPicker picker) {
        super(title, DISPOSE_ON_CLOSE);
        this.picker = picker;
        viewer.setBackground(new Color(picker.rgb));
        r.setValue(picker.r);
        g.setValue(picker.g);
        b.setValue(picker.b);
        a.setValue(picker.a);
        r.addChangeListener(this);
        g.addChangeListener(this);
        b.addChangeListener(this);
        a.addChangeListener(this);
    }

    @Override
    public void createObjects() {
        r = new JSlider(0, 255);
        g = new JSlider(0, 255);
        b = new JSlider(0, 255);
        a = new JSlider(0, 255);
        sliders = new JPanel();
        viewer = new JPanel();
    }

    @Override
    public void configurate() {
        viewer.setPreferredSize(new Dimension(25, 25));
        r.setPreferredSize(new Dimension(350, 50));
        g.setPreferredSize(new Dimension(350, 50));
        b.setPreferredSize(new Dimension(350, 50));
        a.setPreferredSize(new Dimension(350, 50));
        a.setBorder(customTitledBorder("Alpha"));
        g.setBorder(customTitledBorder("Green"));
        b.setBorder(customTitledBorder("Blue"));
        r.setBorder(customTitledBorder("Red"));
        sliders.setLayout(new GridBagLayout());
    }

    @Override
    public void addElements() {
        buttonsBar.add(accept);
        add(viewer);
        sliders.add(r, GBC);
        sliders.add(g, GBC);
        sliders.add(b, GBC);
        sliders.add(a, GBC);
        add(sliders);
        add(buttonsBar);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        viewer.setBackground(new Color(r.getValue(), g.getValue(), b.getValue()));
        picker.setColor(r.getValue(), g.getValue(), b.getValue(), a.getValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == accept) {
            dispose();
        }
    }

}
