/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.FormulaRenderingResult;
import org.workcraft.formula.utils.FormulaToGraphics;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanReplacer;

@Hotkey(KeyEvent.VK_R)
@DisplayName("RhoClause")
@SVGIcon("images/node-rho.svg")
public class VisualRhoClause extends VisualComponent {
    private static float strokeWidth = 0.038f;

    private Rectangle2D boudingBox = new Rectangle2D.Float(0, 0, 0, 0);

    private static Font font;

    static {
        try {
            font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);
        } catch (FontFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public VisualRhoClause(RhoClause rhoClause) {
        super(rhoClause);
        removePropertyDeclarationByName("Name positioning");
        removePropertyDeclarationByName("Name color");
    }

    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Color background = r.getDecoration().getBackground();

        FormulaRenderingResult result = FormulaToGraphics.render(getFormula(), g.getFontRenderContext(), font);

        Rectangle2D textBB = result.boundingBox;

        float textX = (float) -textBB.getCenterX();
        float textY = (float) -textBB.getCenterY();

        float width = (float) textBB.getWidth() + 0.4f;
        float height = (float) textBB.getHeight() + 0.2f;

        boudingBox = new Rectangle2D.Float(-width / 2, -height / 2, width, height);

        g.setStroke(new BasicStroke(strokeWidth));

        g.setColor(Coloriser.colorise(getFillColor(), background));
        g.fill(boudingBox);
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.draw(boudingBox);

        AffineTransform transform = g.getTransform();
        g.translate(textX, textY);
        g.setColor(Coloriser.colorise(getColor(), colorisation));
        result.draw(g);

        g.setTransform(transform);
    }

    private Color getColor() {
        BooleanFormula value = evaluate();
        if (value == One.instance()) {
            return new Color(0x00cc00);
        } else {
            if (value == Zero.instance()) {
                return Color.RED;
            } else {
                return getForegroundColor();
            }
        }
    }

    private BooleanFormula evaluate() {
        return getFormula().accept(new PrettifyBooleanReplacer());
    }

    public Rectangle2D getBoundingBoxInLocalSpace() {
        return boudingBox;
    }

    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    public RhoClause getMathRhoClause() {
        return (RhoClause) getReferencedComponent();
    }

    public BooleanFormula getFormula() {
        return getMathRhoClause().getFormula();
    }

    public void setFormula(BooleanFormula formula) {
        getMathRhoClause().setFormula(formula);
    }
}
