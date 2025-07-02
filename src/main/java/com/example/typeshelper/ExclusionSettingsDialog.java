package com.example.typeshelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExclusionSettingsDialog extends JDialog {

    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private boolean confirmed = false;

    public ExclusionSettingsDialog(Frame owner, List<String> currentExclusions) {
        super(owner, "Filters", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] allExclusions = {
                "_ColorBase", "_Base", "_SoundShader", "_SoundSet", "_Buttstock_ (приклад)", "_Hndgrd_ (рукоять)",
                "_Bipod_ (сошки)", "_Bttstck_ (приклад)", "_Buttstock (приклад)", "_Hndgrd (рукоять)", "_Bipod (сошки)", "_Bttstck (приклад)",
                "_Bttstk (приклад)", "_Flashlight (фонарь)", "_Grip (рукоять)", "_Optic (прицел)", "_PistolGrip (пистолетная рукоять)", "_Supp (глушитель)",
                "_Suppressor (глушитель)", "_VerticalGrip (вертикальная рукоять)", "_RailAK (рукоять АК)", "_Muzzle_AK_", "_ReceiverCover_",
                "_base", "_Adapter", "_Ammo", "Bttstck"
        };

        for (String excl : allExclusions) {
            JCheckBox cb = new JCheckBox(excl);
            cb.setSelected(currentExclusions.contains(excl));
            checkBoxes.add(cb);
            panel.add(cb);
        }

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");

        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getSelectedExclusions() {
        List<String> selected = new ArrayList<>();
        for (JCheckBox cb : checkBoxes) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        return selected;
    }
}
