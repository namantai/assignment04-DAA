#!/usr/bin/env python3
"""Render this project's REPORT.md and figures as a compact four-page A4 PDF."""
import re
from pathlib import Path
from xml.sax.saxutils import escape

import reportlab
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Image, PageBreak, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

ROOT = Path(__file__).resolve().parents[1]
WIDTH = A4[0] - 84
font_directory = Path(reportlab.__file__).parent / "fonts"
for name, filename in (("ReportSans", "Vera.ttf"), ("ReportSans-Bold", "VeraBd.ttf"),
                       ("ReportSans-Italic", "VeraIt.ttf"), ("ReportSans-BoldItalic", "VeraBI.ttf")):
    pdfmetrics.registerFont(TTFont(name, str(font_directory / filename)))
pdfmetrics.registerFontFamily("ReportSans", normal="ReportSans", bold="ReportSans-Bold",
                              italic="ReportSans-Italic", boldItalic="ReportSans-BoldItalic")
styles = getSampleStyleSheet()
styles.add(ParagraphStyle("ReportBody", fontName="ReportSans", fontSize=9.5, leading=12.5,
                          spaceAfter=6, alignment=TA_LEFT))
styles.add(ParagraphStyle("ReportTitle", fontName="ReportSans-Bold", fontSize=14, leading=17, spaceAfter=8))
styles.add(ParagraphStyle("ReportHeading", fontName="ReportSans-Bold", fontSize=12, leading=15,
                          spaceBefore=7, spaceAfter=5, keepWithNext=True))
styles.add(ParagraphStyle("ReportQuestion", fontName="ReportSans-Bold", fontSize=10, leading=13,
                          spaceBefore=7, spaceAfter=4, keepWithNext=True))
styles.add(ParagraphStyle("ReportCell", fontName="ReportSans", fontSize=8.4, leading=10.5))
styles.add(ParagraphStyle("ReportCaption", fontName="ReportSans-Italic", fontSize=9, leading=11.5, spaceAfter=7))


def inline(text):
    value = escape(text)
    value = re.sub(r"`([^`]+)`", r'<font name="Courier">\1</font>', value)
    value = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", value)
    return value


def footer(canvas, doc):
    canvas.setStrokeColor(colors.HexColor("#CCD3DC"))
    canvas.line(42, 32, A4[0] - 42, 32)
    canvas.setFont("ReportSans", 8)
    canvas.setFillColor(colors.HexColor("#526070"))
    canvas.drawString(42, 20, "Nurtas Amantay | SE-2506 | DAA Assignment 4")
    canvas.drawRightString(A4[0] - 42, 20, str(doc.page))


def main():
    lines = (ROOT / "REPORT.md").read_text().splitlines()
    story = []
    i = 0
    while i < len(lines):
        text = lines[i].strip()
        if not text:
            i += 1
            continue
        if text == "<!-- pagebreak -->":
            story.append(PageBreak())
        elif text.startswith("|"):
            table_rows = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                cells = [cell.strip() for cell in lines[i].strip().strip("|").split("|")]
                if not all(re.fullmatch(r"[-:]+", cell) for cell in cells):
                    table_rows.append([Paragraph(inline(cell), styles["ReportCell"]) for cell in cells])
                i += 1
            table = Table(table_rows, colWidths=[58, (WIDTH - 58) / 2, (WIDTH - 58) / 2], repeatRows=1)
            table.setStyle(TableStyle([
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E3EBF4")),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F7F9FB")]),
                ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#C9D2DD")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 5), ("RIGHTPADDING", (0, 0), (-1, -1), 5),
                ("TOPPADDING", (0, 0), (-1, -1), 4), ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
            ]))
            story.extend([table, Spacer(1, 7)])
            continue
        elif text.startswith("!["):
            match = re.fullmatch(r"!\[.*?\]\((.*?)\)", text)
            if not match:
                raise ValueError("Invalid image: " + text)
            path = ROOT / match.group(1)
            width, height = ImageReader(str(path)).getSize()
            figure = Image(str(path), width=WIDTH, height=WIDTH * height / width)
            figure.keepWithNext = True
            story.extend([figure, Spacer(1, 4)])
        elif text.startswith("#"):
            level = len(text) - len(text.lstrip("#"))
            style = {1: "ReportTitle", 2: "ReportHeading", 3: "ReportQuestion"}[level]
            story.append(Paragraph(inline(text[level:].strip()), styles[style]))
        else:
            paragraph = [text]
            while i + 1 < len(lines) and lines[i + 1].strip() and not lines[i + 1].startswith(("#", "|", "![", "<!--")):
                i += 1
                paragraph.append(lines[i].strip())
            style = "ReportCaption" if text.startswith("Figure ") else "ReportBody"
            story.append(Paragraph(inline(" ".join(paragraph)), styles[style]))
        i += 1
    document = SimpleDocTemplate(str(ROOT / "REPORT.pdf"), pagesize=A4,
                                 rightMargin=42, leftMargin=42, topMargin=34, bottomMargin=42,
                                 title="DAA Assignment 4 - Dynamic Programming and String Algorithms",
                                 author="Nurtas Amantay")
    document.build(story, onFirstPage=footer, onLaterPages=footer)
    print(ROOT / "REPORT.pdf")


if __name__ == "__main__":
    main()
