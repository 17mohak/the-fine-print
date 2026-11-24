"""Small python-docx helpers used by build_documents.py (inline markup, tables, code blocks, figures, footer)."""
import re

from docx import Document
from docx.enum.section import WD_ORIENT  # noqa: F401  (kept for completeness)
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_COLOR_INDEX
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor

ACCENT = RGBColor(0x7A, 0x2E, 0x14)
GREY = RGBColor(0x44, 0x48, 0x50)
BODY_FONT = "Calibri"
CODE_FONT = "Consolas"

TOKEN = re.compile(r"(\*\*.+?\*\*|`.+?`|\[\[.+?\]\]|(?<![\w*])\*[^*\s][^*]*?\*(?![\w*])|_.+?_)")


def new_document(body_pt=10.5, margin_in=0.8):
    doc = Document()
    sec = doc.sections[0]
    sec.page_width, sec.page_height = Inches(8.27), Inches(11.69)  # A4
    for side in ("left_member", "right_member"):
        pass
    sec.left_margin = sec.right_margin = Inches(margin_in)
    sec.top_margin = Inches(margin_in)
    sec.bottom_margin = Inches(margin_in)
    st = doc.styles["Normal"]
    st.font.name = BODY_FONT
    st.font.size = Pt(body_pt)
    st.element.rPr.rFonts.set(qn("w:eastAsia"), BODY_FONT)
    st.element.rPr.rFonts.set(qn("w:cs"), "Mangal")  # Devanagari-capable complex-script font on Windows
    st.paragraph_format.space_after = Pt(4)
    st.paragraph_format.space_before = Pt(0)
    st.paragraph_format.line_spacing = 1.05
    for name, size, before, after in (("Heading 1", 13, 10, 3), ("Heading 2", 11, 6, 2), ("Title", 19, 0, 2)):
        h = doc.styles[name]
        h.font.name = BODY_FONT
        h.font.size = Pt(size)
        h.font.bold = True
        h.font.color.rgb = ACCENT
        h.element.rPr.rFonts.set(qn("w:ascii"), BODY_FONT)
        h.element.rPr.rFonts.set(qn("w:hAnsi"), BODY_FONT)
        h.paragraph_format.space_before = Pt(before)
        h.paragraph_format.space_after = Pt(after)
        h.paragraph_format.keep_with_next = True
    return doc


def _add_runs(par, text, size=None, color=None, italic_all=False):
    for part in TOKEN.split(text):
        if not part:
            continue
        run = None
        if part.startswith("**") and part.endswith("**"):
            run = par.add_run(part[2:-2]); run.bold = True
        elif part.startswith("`") and part.endswith("`"):
            run = par.add_run(part[1:-1]); run.font.name = CODE_FONT
            run._element.rPr.rFonts.set(qn("w:eastAsia"), CODE_FONT)
            run.font.size = Pt((size or 10.5) - 1)
        elif part.startswith("[[") and part.endswith("]]"):
            run = par.add_run(part[2:-2]); run.font.highlight_color = WD_COLOR_INDEX.YELLOW
        elif (part.startswith("_") and part.endswith("_") and len(part) > 2) or (part.startswith("*") and part.endswith("*") and len(part) > 2):
            run = par.add_run(part[1:-1]); run.italic = True
        else:
            run = par.add_run(part)
        if size and not (part.startswith("`")):
            run.font.size = Pt(size)
        if color is not None:
            run.font.color.rgb = color
        if italic_all:
            run.italic = True
    return par


def para(doc, text, size=None, align=None, after=None, before=None, color=None, italic=False, keep_next=False):
    p = doc.add_paragraph()
    _add_runs(p, text, size, color, italic)
    if align == "center":
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if after is not None:
        p.paragraph_format.space_after = Pt(after)
    if before is not None:
        p.paragraph_format.space_before = Pt(before)
    if keep_next:
        p.paragraph_format.keep_with_next = True
    return p


def heading(doc, text, level=1):
    return doc.add_heading(text, level=level)


def bullets(doc, items, size=None, after=1.5):
    for it in items:
        p = doc.add_paragraph(style="List Bullet")
        _add_runs(p, it, size)
        p.paragraph_format.space_after = Pt(after)
        p.paragraph_format.left_indent = Inches(0.22)
        p.paragraph_format.first_line_indent = Inches(-0.18)


def numbered(doc, items, size=None, after=1.5):
    for it in items:
        p = doc.add_paragraph(style="List Number")
        _add_runs(p, it, size)
        p.paragraph_format.space_after = Pt(after)


def _shade(cell, hex_fill):
    tcPr = cell._element.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear"); shd.set(qn("w:color"), "auto"); shd.set(qn("w:fill"), hex_fill)
    tcPr.append(shd)


def _cell_margins(table, top=30, bottom=30, left=70, right=70):
    tblPr = table._element.tblPr
    mar = OxmlElement("w:tblCellMar")
    for side, val in (("top", top), ("left", left), ("bottom", bottom), ("right", right)):
        e = OxmlElement(f"w:{side}"); e.set(qn("w:w"), str(val)); e.set(qn("w:type"), "dxa"); mar.append(e)
    tblPr.append(mar)


def table(doc, rows, widths_in, header=True, size=9, first_col_bold=False, align_right_cols=()):
    t = doc.add_table(rows=len(rows), cols=len(rows[0]))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    _cell_margins(t)
    for i, row in enumerate(rows):
        for j, text in enumerate(row):
            c = t.cell(i, j)
            c.width = Inches(widths_in[j])
            p = c.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.0
            _add_runs(p, str(text), size)
            if j in align_right_cols and i > 0:
                p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
            if header and i == 0:
                for r in p.runs:
                    r.bold = True
                _shade(c, "EADBD2")
            elif first_col_bold and j == 0:
                for r in p.runs:
                    r.bold = True
    # repeat header row
    if header:
        trPr = t.rows[0]._tr.get_or_add_trPr()
        h = OxmlElement("w:tblHeader"); h.set(qn("w:val"), "true"); trPr.append(h)
    for row in t.rows:  # do not split rows across pages
        trPr = row._tr.get_or_add_trPr()
        cs = OxmlElement("w:cantSplit"); cs.set(qn("w:val"), "true"); trPr.append(cs)
    gap = doc.add_paragraph(); gap.paragraph_format.space_after = Pt(2)
    gap.paragraph_format.line_spacing = 0.5
    return t


def code_block(doc, text, size=8.3):
    lines = text.strip("\n").split("\n")
    for k, line in enumerate(lines):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.left_indent = Inches(0.1)
        p.paragraph_format.keep_with_next = k < len(lines) - 1
        pPr = p._p.get_or_add_pPr()
        shd = OxmlElement("w:shd"); shd.set(qn("w:val"), "clear"); shd.set(qn("w:color"), "auto"); shd.set(qn("w:fill"), "F4EFE8")
        pPr.append(shd)
        r = p.add_run(line if line else " ")
        r.font.name = CODE_FONT
        r._element.rPr.rFonts.set(qn("w:eastAsia"), CODE_FONT)
        r.font.size = Pt(size)
    sp = doc.add_paragraph(); sp.paragraph_format.space_after = Pt(2); sp.paragraph_format.line_spacing = 0.5


def figure(doc, path, width_in, caption):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(1)
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(str(path), width=Inches(width_in))
    c = doc.add_paragraph()
    c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    _add_runs(c, caption, 8.8, GREY, italic_all=True)
    c.paragraph_format.space_after = Pt(5)


def _field(run, instr):
    for kind, text in (("begin", None), (None, instr), ("end", None)):
        if kind:
            e = OxmlElement("w:fldChar"); e.set(qn("w:fldCharType"), kind); run._r.append(e)
        else:
            e = OxmlElement("w:instrText"); e.set(qn("xml:space"), "preserve"); e.text = text; run._r.append(e)


def footer(doc, left_text):
    sec = doc.sections[0]
    p = sec.footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(left_text + "   |   Page "); r.font.size = Pt(8); r.font.color.rgb = GREY
    r2 = p.add_run(); r2.font.size = Pt(8); r2.font.color.rgb = GREY; _field(r2, "PAGE")
    r3 = p.add_run(" of "); r3.font.size = Pt(8); r3.font.color.rgb = GREY
    r4 = p.add_run(); r4.font.size = Pt(8); r4.font.color.rgb = GREY; _field(r4, "NUMPAGES")


def rule(doc):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(3)
    pPr = p._p.get_or_add_pPr()
    b = OxmlElement("w:pBdr"); bt = OxmlElement("w:bottom")
    bt.set(qn("w:val"), "single"); bt.set(qn("w:sz"), "8"); bt.set(qn("w:space"), "1"); bt.set(qn("w:color"), "7A2E14")
    b.append(bt); pPr.append(b)
