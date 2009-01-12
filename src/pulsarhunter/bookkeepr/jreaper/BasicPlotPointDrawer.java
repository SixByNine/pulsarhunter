/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.bookkeepr.jreaper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import pulsarhunter.jreaper.CandClass;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author kei041
 */
public class BasicPlotPointDrawer implements PlotPointDrawer {

    private static final Color trans127_red = new Color(255, 0, 0, 127);
    private static final Color trans127_green = new Color(0, 255, 0, 127);
    private static final Color trans127_blue = new Color(0, 0, 255, 127);
    private static final Color trans32_red = new Color(255, 0, 0, 32);
    private static final Color trans32_green = new Color(0, 255, 0, 32);

    public BasicPlotPointDrawer() {
    }

    public void drawPoint(Graphics graphics, int x, int y, CandClass cl, HarmonicType htype, boolean possibleMatch, int zcolor, boolean viewed) {
        Color color;
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (zcolor >= 0) {
            color = colourZaxis[255 - zcolor];
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);
            graphics.setColor(color);

            graphics.fillOval(x - 6, y - 6, 12, 12);
        }
        color = new Color(255, 0, 0, 32);

        if (cl == CandClass.Class1) {
            color = Color.BLACK;
        }
        if (cl == CandClass.Class2) {
            color = Color.GRAY;
        }
        if (cl == CandClass.Class3) {
            color = Color.LIGHT_GRAY;
        }
        if (cl == CandClass.KnownPsr) {
            color = Color.ORANGE;
        }
        graphics.setColor(color);
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);

        if (possibleMatch) {


            if (htype == HarmonicType.Principal) {
                graphics.fillPolygon(new int[]{x, x - 5, x, x + 5}, new int[]{y + 5, y, y - 5, y}, 4);
            } else {
                graphics.drawLine(x + 3, y - 3, x - 3,
                        y + 3);
                graphics.drawLine(x - 3, y - 3, x + 3,
                        y + 3);

            }

        } else if (htype == HarmonicType.None) {
//            graphics.drawLine(x + 4, y - 4, x - 4,
//                    y + 4);
//            graphics.drawLine(x - 4, y - 4, x + 4,
//                    y + 4);
        } else if (htype == HarmonicType.Principal) {
            graphics.fillOval(x - 4, y - 4, 8, 8);
        } else if (htype == HarmonicType.Integer) {
            graphics.fillPolygon(new int[]{-4 + x, 4 + x, 4 + x}, new int[]{0 + y, 4 + y, -4 + y}, 3);
        } else if (htype == HarmonicType.SimpleNonInteger) {
            graphics.fillPolygon(new int[]{4 + x, -4 + x, -4 + x}, new int[]{0 + y, 4 + y, -4 + y}, 3);
        } else if (htype == HarmonicType.ComplexNonInteger) {
            graphics.fillPolygon(new int[]{x, -4 + x, -4 + x}, new int[]{0 + y, 4 + y, -4 + y}, 3);
            graphics.fillPolygon(new int[]{x, 4 + x, 4 + x}, new int[]{0 + y, 4 + y, -4 + y}, 3);
        }


        color = new Color(102, 0, 204, 160);
        if (zcolor >= 0) {
            color = colourZaxis[255 - zcolor];

        }
        //color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 192);

        graphics.setColor(color);


        if (viewed) {
            graphics.fillRect(x - 1, y - 1, 3, 3);


        } else {
            graphics.drawLine(x, y - 3, x,
                    y + 3);
            graphics.drawLine(x - 3, y, x + 3,
                    y);
            graphics.fillRect(x - 1, y - 1, 3, 3);
        }
    }

    public String getName() {
        return "Basic";
    }
    private static Color[] colourZaxis = {
        new Color(0xff0000),
        new Color(0xfd0200),
        new Color(0xfb0400),
        new Color(0xf90600),
        new Color(0xf70800),
        new Color(0xf50a00),
        new Color(0xf30c00),
        new Color(0xf10e00),
        new Color(0xef1000),
        new Color(0xed1200),
        new Color(0xeb1400),
        new Color(0xe91600),
        new Color(0xe71800),
        new Color(0xe51a00),
        new Color(0xe31c00),
        new Color(0xe11e00),
        new Color(0xdf2000),
        new Color(0xdd2200),
        new Color(0xdb2400),
        new Color(0xd92600),
        new Color(0xd72800),
        new Color(0xd52a00),
        new Color(0xd32c00),
        new Color(0xd12e00),
        new Color(0xcf3000),
        new Color(0xcd3200),
        new Color(0xcb3400),
        new Color(0xc93600),
        new Color(0xc73800),
        new Color(0xc53a00),
        new Color(0xc33c00),
        new Color(0xc13e00),
        new Color(0xbf4000),
        new Color(0xbd4200),
        new Color(0xbb4400),
        new Color(0xb94600),
        new Color(0xb74800),
        new Color(0xb54a00),
        new Color(0xb34c00),
        new Color(0xb14e00),
        new Color(0xaf5000),
        new Color(0xad5200),
        new Color(0xab5400),
        new Color(0xa95600),
        new Color(0xa75800),
        new Color(0xa55a00),
        new Color(0xa35c00),
        new Color(0xa15e00),
        new Color(0x9f6000),
        new Color(0x9d6200),
        new Color(0x9b6400),
        new Color(0x996600),
        new Color(0x976800),
        new Color(0x956a00),
        new Color(0x936c00),
        new Color(0x916e00),
        new Color(0x8f7000),
        new Color(0x8d7200),
        new Color(0x8b7400),
        new Color(0x897600),
        new Color(0x877800),
        new Color(0x857a00),
        new Color(0x837c00),
        new Color(0x817e00),
        new Color(0x7f8000),
        new Color(0x7d8200),
        new Color(0x7b8400),
        new Color(0x798600),
        new Color(0x778800),
        new Color(0x758a00),
        new Color(0x738c00),
        new Color(0x718e00),
        new Color(0x6f9000),
        new Color(0x6d9200),
        new Color(0x6b9400),
        new Color(0x699600),
        new Color(0x679800),
        new Color(0x659a00),
        new Color(0x639c00),
        new Color(0x619e00),
        new Color(0x5fa000),
        new Color(0x5da200),
        new Color(0x5ba400),
        new Color(0x59a600),
        new Color(0x57a800),
        new Color(0x55aa00),
        new Color(0x53ac00),
        new Color(0x51ae00),
        new Color(0x4fb000),
        new Color(0x4db200),
        new Color(0x4bb400),
        new Color(0x49b600),
        new Color(0x47b800),
        new Color(0x45ba00),
        new Color(0x43bc00),
        new Color(0x41be00),
        new Color(0x3fc000),
        new Color(0x3dc200),
        new Color(0x3bc400),
        new Color(0x39c600),
        new Color(0x37c800),
        new Color(0x35ca00),
        new Color(0x33cc00),
        new Color(0x31ce00),
        new Color(0x2fd000),
        new Color(0x2dd200),
        new Color(0x2bd400),
        new Color(0x29d600),
        new Color(0x27d800),
        new Color(0x25da00),
        new Color(0x23dc00),
        new Color(0x21de00),
        new Color(0x1fe000),
        new Color(0x1de200),
        new Color(0x1be400),
        new Color(0x19e600),
        new Color(0x17e800),
        new Color(0x15ea00),
        new Color(0x13ec00),
        new Color(0x11ee00),
        new Color(0x0ff000),
        new Color(0x0df200),
        new Color(0x0bf400),
        new Color(0x09f600),
        new Color(0x07f800),
        new Color(0x05fa00),
        new Color(0x03fc00),
        new Color(0x01fe00),
        new Color(0x00ff00),
        new Color(0x00fd02),
        new Color(0x00fb04),
        new Color(0x00f906),
        new Color(0x00f708),
        new Color(0x00f50a),
        new Color(0x00f30c),
        new Color(0x00f10e),
        new Color(0x00ef10),
        new Color(0x00ed12),
        new Color(0x00eb14),
        new Color(0x00e916),
        new Color(0x00e718),
        new Color(0x00e51a),
        new Color(0x00e31c),
        new Color(0x00e11e),
        new Color(0x00df20),
        new Color(0x00dd22),
        new Color(0x00db24),
        new Color(0x00d926),
        new Color(0x00d728),
        new Color(0x00d52a),
        new Color(0x00d32c),
        new Color(0x00d12e),
        new Color(0x00cf30),
        new Color(0x00cd32),
        new Color(0x00cb34),
        new Color(0x00c936),
        new Color(0x00c738),
        new Color(0x00c53a),
        new Color(0x00c33c),
        new Color(0x00c13e),
        new Color(0x00bf40),
        new Color(0x00bd42),
        new Color(0x00bb44),
        new Color(0x00b946),
        new Color(0x00b748),
        new Color(0x00b54a),
        new Color(0x00b34c),
        new Color(0x00b14e),
        new Color(0x00af50),
        new Color(0x00ad52),
        new Color(0x00ab54),
        new Color(0x00a956),
        new Color(0x00a758),
        new Color(0x00a55a),
        new Color(0x00a35c),
        new Color(0x00a15e),
        new Color(0x009f60),
        new Color(0x009d62),
        new Color(0x009b64),
        new Color(0x009966),
        new Color(0x009768),
        new Color(0x00956a),
        new Color(0x00936c),
        new Color(0x00916e),
        new Color(0x008f70),
        new Color(0x008d72),
        new Color(0x008b74),
        new Color(0x008976),
        new Color(0x008778),
        new Color(0x00857a),
        new Color(0x00837c),
        new Color(0x00817e),
        new Color(0x007f80),
        new Color(0x007d82),
        new Color(0x007b84),
        new Color(0x007986),
        new Color(0x007788),
        new Color(0x00758a),
        new Color(0x00738c),
        new Color(0x00718e),
        new Color(0x006f90),
        new Color(0x006d92),
        new Color(0x006b94),
        new Color(0x006996),
        new Color(0x006798),
        new Color(0x00659a),
        new Color(0x00639c),
        new Color(0x00619e),
        new Color(0x005fa0),
        new Color(0x005da2),
        new Color(0x005ba4),
        new Color(0x0059a6),
        new Color(0x0057a8),
        new Color(0x0055aa),
        new Color(0x0053ac),
        new Color(0x0051ae),
        new Color(0x004fb0),
        new Color(0x004db2),
        new Color(0x004bb4),
        new Color(0x0049b6),
        new Color(0x0047b8),
        new Color(0x0045ba),
        new Color(0x0043bc),
        new Color(0x0041be),
        new Color(0x003fc0),
        new Color(0x003dc2),
        new Color(0x003bc4),
        new Color(0x0039c6),
        new Color(0x0037c8),
        new Color(0x0035ca),
        new Color(0x0033cc),
        new Color(0x0031ce),
        new Color(0x002fd0),
        new Color(0x002dd2),
        new Color(0x002bd4),
        new Color(0x0029d6),
        new Color(0x0027d8),
        new Color(0x0025da),
        new Color(0x0023dc),
        new Color(0x0021de),
        new Color(0x001fe0),
        new Color(0x001de2),
        new Color(0x001be4),
        new Color(0x0019e6),
        new Color(0x0017e8),
        new Color(0x0015ea),
        new Color(0x0013ec),
        new Color(0x0011ee),
        new Color(0x000ff0),
        new Color(0x000df2),
        new Color(0x000bf4),
        new Color(0x0009f6),
        new Color(0x0007f8),
        new Color(0x0005fa),
        new Color(0x0003fc),
        new Color(0x0001fe),
    };
}
