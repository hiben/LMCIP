/**
 LMCIP - Let Me Copy In Peace
 Copyright 2022 Hendrik Iben

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package de.zvxeb;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;

public class LMCIP implements Runnable {

    private BufferedImage iconEnabled;
    private BufferedImage iconDisabled;
    private TrayIcon ti;
    private static SystemTray systemTray;

    private boolean pacifyClipboard = true;
    private boolean keepRunning = true;

    CheckboxMenuItem enabledCheckbox;

    private void updateState() {
        ti.setImage(pacifyClipboard ? iconEnabled : iconDisabled);
        ti.setToolTip(String.format("Let Me Copy In Peace%s", pacifyClipboard ? "" : " (disabled)"));
    }

    private void createTrayIcon() {
        ti = new TrayIcon(iconEnabled, "Let Me Copy In Peace");
        ti.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() >= 2) {
                    pacifyClipboard = !pacifyClipboard;
                    enabledCheckbox.setState(pacifyClipboard);
                    updateState();
                }
            }
        });
        PopupMenu pm = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        enabledCheckbox = new CheckboxMenuItem("Enabled", pacifyClipboard);
        enabledCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                pacifyClipboard = enabledCheckbox.getState();
                updateState();
            }
        });
        exitItem.addActionListener((e) -> {
            systemTray.remove(ti);
            System.exit(0);
        });
        pm.add(enabledCheckbox);
        pm.addSeparator();
        pm.add(exitItem);
        ti.setPopupMenu(pm);
    }

    private void createIcons() {
        BufferedImage bimg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bimg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setBackground(new Color(0x00FF0000, true));
        g2d.setColor(Color.white);
        g2d.clearRect(0,0,16,16);
        // Boxes + V
        g2d.drawPolyline(new int [] {4, 4, 14,  14, 4, 4,  1, 1, 11,  11}, new int [] {11, 14, 14,  4, 4, 11,  11, 1, 1,  4}, 10);
        g2d.drawPolyline(new int [] {3, 3}, new int [] {3, 5}, 2);
        g2d.drawPolyline(new int [] {6, 9, 12}, new int [] {6, 12, 6}, 3);

        iconEnabled = bimg;
        iconDisabled = new BufferedImage(iconEnabled.getWidth(), iconEnabled.getHeight(), bimg.getType());
        g2d = iconDisabled.createGraphics();
        g2d.setBackground(new Color(0x00FF0000, true));
        g2d.setColor(Color.white);
        g2d.clearRect(0,0,16,16);
        iconDisabled.createGraphics().drawImage(iconEnabled, null, 0, 0);
        g2d.drawPolyline(new int [] {0, 15}, new int [] {0, 15}, 2);
        g2d.drawPolyline(new int [] {0, 15}, new int [] {15, 0}, 2);
    }

    public void run() {
        createIcons();
        createTrayIcon();
        if(ti!=null) {
            try {
                systemTray.add(ti);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                while(keepRunning) {
                    if(pacifyClipboard) {
                        DataFlavor[] flavors = clipboard.getAvailableDataFlavors();
                        boolean hasText = false;
                        boolean hasFormatted = false;
                        for (DataFlavor df : flavors) {
                            String mimeType = df.getMimeType();
                            if (
                                    mimeType.startsWith("text/html") ||
                                            mimeType.startsWith("text/rtf")
                            ) {
                                hasFormatted = true;
                                break;
                            }
                            if (!hasText && mimeType.startsWith("text/plain")) {
                                hasText = true;
                            }
                        }

                        if (hasFormatted) {
                            Transferable t = clipboard.getContents(DataFlavor.stringFlavor);
                            try {
                                Reader stringReader = DataFlavor.stringFlavor.getReaderForText(t);
                                char [] buffer = new char [1024];
                                int r;
                                StringBuilder sb = new StringBuilder();
                                while( (r = stringReader.read(buffer)) > 0) {
                                    sb.append(new String(buffer, 0, r));
                                }
                                clipboard.setContents(new StringSelection(sb.toString()), null);
                            } catch (UnsupportedFlavorException e) {
                            } catch (IOException e) {
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }

            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String...args) {
        try {
            systemTray = SystemTray.getSystemTray();
            new LMCIP().run();
        } catch(Exception e) {
            System.err.format("Unable to access system tray: %s\n", e.getMessage());
            System.exit(1);
        }
    }
}
