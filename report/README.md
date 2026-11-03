# Integrated report

`SC2002_Restaurant_Rush_Report.pdf` is the current integration working copy. It describes PR #1's
Java code based on `b62d196`, plus the final order-announcement fix, using an adapted report structure from PR #2 (`941ecc9`).
The report does not claim that group identities, personal reflections, a recording, declarations
or final remote stage tags have been completed.

- `latex/report.tex`: authored report source; edit supporting sections in the same directory.
- `SC2002_Restaurant_Rush_Report.html`: local browser-print edition used for the supplied PDF, including vector diagrams.
- `diagrams/class-*.svg` and `diagrams/sequence-*.svg`: exported editable diagram geometry.
- `diagrams/class_diagram.pdf`: complementary final class views.
- `diagrams/sequence_diagram.pdf`: complete VIP/Happy Hour scenario in panels.
- `../docs/DESIGN.md`: editable Mermaid views and matching design notes.
- `../TESTING.md` and `../tests/evidence/results.json`: freshly regenerated functional evidence and the pinned working-source hash. The patch adds no class or API, so diagram geometry is unchanged.

The LaTeX project compiled successfully with the built-in editor. The supplied PDF is the
locally printed HTML edition; its SVGs reproduce the authored TikZ diagram geometry.
Open the HTML in a browser and print to PDF to reproduce that edition without installing TeX.

Open `latex/report.tex` in the Codex LaTeX editor for source editing and PDF preview.
With an installed TeX distribution, run `xelatex report.tex` twice from `report/latex/` and copy
`report.pdf` to `report/SC2002_Restaurant_Rush_Report.pdf`. Standalone diagram PDFs are extracts
of the report's diagram pages; regenerate them when the report diagrams change.

Before final submission, fill actual group information, individual reflections, video/link and
timestamps, attach the signed course forms, and verify the agreed published stage tags.
