"""Lee PAGO BOLETOS 2026.xlsx (fórmulas, no caché) y escribe tarifas.tsv.

El neto se calcula aquí: precio - descuento - sitio - plata.
La plata vacía significa que esa base no la usa.
"""

from __future__ import annotations

import re
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

import openpyxl

XLSX = Path(r"C:\Users\carlo\Downloads\PAGO BOLETOS 2026.xlsx")
OUT = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "boletos" / "tarifas.tsv"

TOKEN = re.compile(
    r"\s+|"
    r"'(?:[^']|'')+'!|"
    r"[A-Za-z0-9_.][A-Za-z0-9_. ]*!|"
    r"\$?[A-Z]{1,3}\$?\d+|"
    r"\d+(?:\.\d+)?|"
    r"[()+\-*/]"
)


def money(value: Decimal | None) -> Decimal | None:
    if value is None:
        return None
    return value.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def to_decimal(value):
    if value is None or value == "":
        return None
    if isinstance(value, bool):
        return None
    if isinstance(value, Decimal):
        return value
    if isinstance(value, int):
        return Decimal(value)
    if isinstance(value, float):
        return Decimal(str(value))
    if isinstance(value, str):
        text = value.strip()
        if not text:
            return None
        return Decimal(text)
    raise TypeError(type(value))


class Book:
    def __init__(self, path: Path):
        self.wb = openpyxl.load_workbook(path, data_only=False)
        self.cache: dict[tuple[str, str], Decimal | None] = {}
        self.stack: set[tuple[str, str]] = set()

    def get(self, sheet: str, coord: str) -> Decimal | None:
        key = (sheet, coord.replace("$", ""))
        if key in self.cache:
            return self.cache[key]
        if key in self.stack:
            raise RuntimeError(f"ciclo en {sheet}!{coord}")
        self.stack.add(key)
        cell = self.wb[sheet][key[1]]
        raw = cell.value
        if isinstance(raw, str) and raw.startswith("="):
            result = self.eval_formula(sheet, raw[1:])
        elif isinstance(raw, str):
            result = None
        else:
            result = to_decimal(raw)
        self.cache[key] = result
        self.stack.remove(key)
        return result

    def text(self, sheet: str, coord: str) -> str:
        raw = self.wb[sheet][coord].value
        if raw is None:
            return ""
        return str(raw).strip()

    def eval_formula(self, sheet: str, formula: str) -> Decimal | None:
        tokens = [token for token in TOKEN.findall(formula) if not token.isspace()]
        if not tokens:
            return None
        pos = 0

        def peek():
            return tokens[pos] if pos < len(tokens) else None

        def take():
            nonlocal pos
            token = tokens[pos]
            pos += 1
            return token

        def parse_expr():
            value = parse_term()
            while peek() in ("+", "-"):
                op = take()
                right = parse_term()
                if value is None or right is None:
                    value = None
                elif op == "+":
                    value = value + right
                else:
                    value = value - right
            return value

        def parse_term():
            value = parse_factor()
            while peek() in ("*", "/"):
                op = take()
                right = parse_factor()
                if value is None or right is None:
                    value = None
                elif op == "*":
                    value = value * right
                else:
                    value = value / right
            return value

        def parse_factor():
            token = peek()
            if token == "-":
                take()
                value = parse_factor()
                return None if value is None else -value
            if token == "+":
                take()
                return parse_factor()
            if token == "(":
                take()
                value = parse_expr()
                if peek() != ")":
                    raise RuntimeError(f"falta ) en {sheet}: {formula}")
                take()
                return value
            take()
            if token is None:
                raise RuntimeError(formula)
            if re.fullmatch(r"\d+(?:\.\d+)?", token):
                return Decimal(token)
            sheet_name = sheet
            ref = token
            if token.endswith("!"):
                sheet_name = token[:-1]
                if sheet_name.startswith("'") and sheet_name.endswith("'"):
                    sheet_name = sheet_name[1:-1].replace("''", "'")
                ref = take()
            ref = ref.replace("$", "")
            if not re.fullmatch(r"[A-Z]{1,3}\d+", ref):
                raise RuntimeError(f"no entiendo {token} en {sheet}: {formula}")
            return self.get(sheet_name, ref)

        value = parse_expr()
        if pos != len(tokens):
            raise RuntimeError(f"sobra {tokens[pos:]} en {sheet}: {formula}")
        return value


def emit(rows, book, sheet, row, zona_col, precio_col, desc_col, sitio_col, plata_col, meta):
    zona = book.text(sheet, f"{zona_col}{row}")
    if not zona or zona.upper() == "ZONA":
        return
    precio = book.get(sheet, f"{precio_col}{row}")
    descuento = book.get(sheet, f"{desc_col}{row}")
    sitio = book.get(sheet, f"{sitio_col}{row}")
    plata = book.get(sheet, f"{plata_col}{row}") if plata_col else None
    if precio is None:
        return
    rows.append(
        {
            **meta,
            "zona": zona,
            "precio": money(precio),
            "descuento": money(descuento or Decimal(0)),
            "sitio": money(sitio or Decimal(0)),
            "plata": money(plata) if plata_col else None,
            "manual": "0",
            "clave": "",
        }
    )


def fill_range(rows, book, sheet, start, end, cols, meta):
    for row in range(start, end + 1):
        emit(rows, book, sheet, row, *cols, meta)


def disambiguate(rows):
    seen: dict[tuple, int] = {}
    for row in rows:
        key = (row["base"], row["vigencia"], row["ambito"], row["tipo"], row["zona"])
        seen[key] = seen.get(key, 0) + 1
        if seen[key] > 1:
            row["zona"] = f"{row['zona']} ({seen[key]})"


def neto(row) -> Decimal:
    value = row["precio"] - row["descuento"] - row["sitio"]
    if row["plata"] is not None:
        value -= row["plata"]
    return money(value)


def find(rows, **wanted):
    for row in rows:
        if all(row[key] == value for key, value in wanted.items()):
            return row
    raise KeyError(wanted)


def fmt(value: Decimal | None) -> str:
    if value is None:
        return ""
    return f"{value:.2f}"


def main():
    book = Book(XLSX)
    rows: list[dict] = []

    for vigencia, sheet in (("2025", "BASE NTE 25 "), ("2026", "BASE NTE 26")):
        fill_range(rows, book, sheet, 3, 15, ("A", "B", "C", "D", "E"),
                   {"base": "NORTE", "vigencia": vigencia, "ambito": "CDMX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 3, 15, ("H", "I", "J", "K", "L"),
                   {"base": "NORTE", "vigencia": vigencia, "ambito": "CDMX", "tipo": "doble"})
        fill_range(rows, book, sheet, 20, 45, ("A", "B", "C", "D", "E"),
                   {"base": "NORTE", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 20, 45, ("H", "I", "J", "K", "L"),
                   {"base": "NORTE", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "nocturno"})

    fill_range(rows, book, "BASE NORTE ESPECIALES", 4, 29, ("A", "B", "C", "D", None),
               {"base": "ESPECIALES", "vigencia": "2026", "ambito": "CDMX", "tipo": "sencillo"})
    fill_range(rows, book, "BASE NORTE ESPECIALES", 4, 29, ("G", "H", "I", "J", None),
               {"base": "ESPECIALES", "vigencia": "2026", "ambito": "CDMX", "tipo": "doble"})
    fill_range(rows, book, "BASE NORTE ESPECIALES", 33, 58, ("A", "B", "C", "D", None),
               {"base": "ESPECIALES", "vigencia": "2026", "ambito": "CDMX", "tipo": "sencillo"})
    fill_range(rows, book, "BASE NORTE ESPECIALES", 33, 58, ("G", "H", "I", "J", None),
               {"base": "ESPECIALES", "vigencia": "2026", "ambito": "CDMX", "tipo": "doble"})

    for vigencia, sheet in (("2025", "BASE SUR 25"), ("2026", "BASE SUR 26")):
        fill_range(rows, book, sheet, 3, 15, ("A", "B", "C", "D", "E"),
                   {"base": "SUR", "vigencia": vigencia, "ambito": "CDMX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 3, 15, ("H", "I", "J", "K", "L"),
                   {"base": "SUR", "vigencia": vigencia, "ambito": "CDMX", "tipo": "doble"})
        fill_range(rows, book, sheet, 19, 35, ("A", "B", "C", "D", "E"),
                   {"base": "SUR", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 19, 35, ("H", "I", "J", "K", "L"),
                   {"base": "SUR", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "doble"})

    for vigencia, sheet in (("2024", "BASE TAPO 24"), ("2025", "BASE TAPO 25")):
        fill_range(rows, book, sheet, 3, 27, ("A", "B", "C", "D", None),
                   {"base": "TAPO", "vigencia": vigencia, "ambito": "CDMX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 3, 26, ("G", "H", "I", "J", None),
                   {"base": "TAPO", "vigencia": vigencia, "ambito": "CDMX", "tipo": "doble"})
        fill_range(rows, book, sheet, 3, 19, ("M", "N", "O", "P", None),
                   {"base": "TAPO", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 23, 39, ("M", "N", "O", "P", None),
                   {"base": "TAPO", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "nocturno"})

    # Catálogo completo REZ (pestaña BASE TAPO REZ).
    fill_range(rows, book, "BASE TAPO REZ", 3, 33, ("A", "B", "C", "D", None),
               {"base": "TAPO", "vigencia": "REZ", "ambito": "CDMX", "tipo": "sencillo"})
    fill_range(rows, book, "BASE TAPO REZ", 3, 33, ("G", "H", "I", "J", None),
               {"base": "TAPO", "vigencia": "REZ", "ambito": "CDMX", "tipo": "doble"})
    fill_range(rows, book, "BASE TAPO REZ", 3, 19, ("M", "N", "O", "P", None),
               {"base": "TAPO", "vigencia": "REZ", "ambito": "EDOMEX", "tipo": "sencillo"})
    fill_range(rows, book, "BASE TAPO REZ", 23, 39, ("M", "N", "O", "P", None),
               {"base": "TAPO", "vigencia": "REZ", "ambito": "EDOMEX", "tipo": "nocturno"})

    # Huecos de REZ que la captura TAPO 25 necesita y BASE TAPO 25 no trae.
    tapo_zones = {
        (row["ambito"], row["tipo"], row["zona"])
        for row in rows
        if row["base"] == "TAPO" and row["vigencia"] == "2025"
    }

    def ya_esta_en_tapo_25(tipo: str, zona: str) -> bool:
        if ("CDMX", tipo, zona) in tapo_zones:
            return True
        # 2 y 2T son la misma zona de BASE TAPO 25. 12A y 32 no tienen pareja ahí.
        if zona.isdigit() and ("CDMX", tipo, f"{zona}T") in tapo_zones:
            return True
        return False

    before = len(rows)
    for tipo, cols, end in (
        ("sencillo", ("A", "B", "C", "D", None), 33),
        ("doble", ("G", "H", "I", "J", None), 33),
    ):
        for row_index in range(3, end + 1):
            zona = book.text("BASE TAPO REZ", f"{cols[0]}{row_index}")
            if not zona or ya_esta_en_tapo_25(tipo, zona):
                continue
            emit(rows, book, "BASE TAPO REZ", row_index, *cols,
                 {"base": "TAPO", "vigencia": "2025", "ambito": "CDMX", "tipo": tipo})
    rez_added = [row["zona"] + " " + row["tipo"] for row in rows[before:]]

    for vigencia, sheet in (("2025", "BASE PTE 25"), ("VIGENTE", "BASE PTE")):
        fill_range(rows, book, sheet, 3, 20, ("A", "B", "C", "D", None),
                   {"base": "PTE", "vigencia": vigencia, "ambito": "CDMX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 3, 20, ("G", "H", "I", "J", None),
                   {"base": "PTE", "vigencia": vigencia, "ambito": "CDMX", "tipo": "doble"})
        fill_range(rows, book, sheet, 24, 49, ("A", "B", "C", "D", None),
                   {"base": "PTE", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "sencillo"})
        fill_range(rows, book, sheet, 24, 49, ("G", "H", "I", "J", None),
                   {"base": "PTE", "vigencia": vigencia, "ambito": "EDOMEX", "tipo": "nocturno"})

    manual_specs = (
        (3, 21, "B", "C", "D", "E", "CDMX", "sencillo"),
        (3, 21, "J", "K", "L", "M", "CDMX", "doble"),
        (26, 38, "B", "C", "D", "E", "EDOMEX", "sencillo"),
        (26, 38, "J", "K", "L", "M", "EDOMEX", "doble"),
    )
    for start, end, zona_col, precio_col, desc_col, sitio_col, ambito, tipo in manual_specs:
        for row_index in range(start, end + 1):
            zona = book.text("MANUALES", f"{zona_col}{row_index}")
            if not zona:
                continue
            precio = book.get("MANUALES", f"{precio_col}{row_index}")
            descuento = book.get("MANUALES", f"{desc_col}{row_index}")
            sitio = book.get("MANUALES", f"{sitio_col}{row_index}")
            rows.append(
                {
                    "base": "MANUAL",
                    "vigencia": "2026",
                    "ambito": ambito,
                    "tipo": tipo,
                    "zona": zona,
                    "precio": money(precio or Decimal(0)),
                    "descuento": money(descuento or Decimal(0)),
                    "sitio": money(sitio or Decimal(0)),
                    "plata": None,
                    "manual": "1",
                    "clave": f"manual-{zona_col}{row_index}",
                }
            )

    disambiguate(rows)
    for index, row in enumerate(rows):
        row["orden"] = index
        row["neto"] = neto(row)

    checks = [
        ("NORTE", "2026", "CDMX", "sencillo", "1", "78.00", "10.00", "6.00", "3.00", "59.00"),
        ("NORTE", "2026", "CDMX", "sencillo", "12", "420.00", "42.00", "20.00", "3.00", "355.00"),
        ("NORTE", "2026", "CDMX", "sencillo", "SAL", "70.00", "22.00", "10.00", "3.00", "35.00"),
        ("NORTE", "2025", "CDMX", "sencillo", "1", "58.00", "8.70", "5.00", "2.36", "41.94"),
        ("NORTE", "2026", "EDOMEX", "sencillo", "A", "137.00", "19.90", "12.70", "2.36", "102.04"),
        ("ESPECIALES", "2026", "CDMX", "sencillo", "AA", "1535.00", "115.12", "38.38", "", "1381.50"),
        ("ESPECIALES", "2026", "CDMX", "sencillo", "PPP", "5015.00", "378.48", "125.38", "", "4511.14"),
        ("SUR", "2026", "CDMX", "sencillo", "1", "78.00", "10.00", "6.00", "3.00", "59.00"),
        ("SUR", "2025", "CDMX", "sencillo", "1", "58.00", "8.70", "5.00", "2.36", "41.94"),
        ("TAPO", "2025", "CDMX", "sencillo", "1", "58.00", "8.10", "4.00", "", "45.90"),
        ("TAPO", "2025", "EDOMEX", "sencillo", "A", "216.00", "17.32", "7.48", "", "191.20"),
        ("TAPO", "2024", "CDMX", "sencillo", "1", "58.00", "8.10", "4.00", "", "45.90"),
        ("TAPO", "REZ", "CDMX", "sencillo", "1", "58.00", "8.10", "6.00", "", "43.90"),
        ("PTE", "VIGENTE", "CDMX", "sencillo", "1", "83.00", "16.00", "5.50", "", "61.50"),
        ("PTE", "2025", "CDMX", "sencillo", "1", "83.00", "15.50", "5.50", "", "62.00"),
        ("MANUAL", "2026", "CDMX", "sencillo", "1", "83.00", "16.00", "5.50", "", "61.50"),
    ]
    for base, vigencia, ambito, tipo, zona, precio, descuento, sitio, plata, expected in checks:
        row = find(rows, base=base, vigencia=vigencia, ambito=ambito, tipo=tipo, zona=zona)
        actual = (
            fmt(row["precio"]),
            fmt(row["descuento"]),
            fmt(row["sitio"]),
            fmt(row["plata"]),
            fmt(row["neto"]),
        )
        wanted = (precio, descuento, sitio, plata, expected)
        if actual != wanted:
            raise SystemExit(f"{base} {vigencia} {ambito} {tipo} {zona}: {actual} != {wanted}")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write("# base vigencia ambito tipo zona precio descuento sitio plata manual clave orden\n")
        handle.write("# Extraído de PAGO BOLETOS 2026.xlsx el 2026-10-21. Fórmulas evaluadas, sin caché.\n")
        for row in rows:
            handle.write(
                "\t".join(
                    [
                        row["base"],
                        row["vigencia"],
                        row["ambito"],
                        row["tipo"],
                        row["zona"],
                        fmt(row["precio"]),
                        fmt(row["descuento"]),
                        fmt(row["sitio"]),
                        fmt(row["plata"]),
                        row["manual"],
                        row["clave"],
                        str(row["orden"]),
                    ]
                )
                + "\n"
            )

    for sample in (
        ("NORTE", "2026", "EDOMEX", "nocturno", "U"),
        ("PTE", "VIGENTE", "CDMX", "doble", "1"),
        ("MANUAL", "2026", "CDMX", "sencillo", "10"),
        ("TAPO", "2025", "CDMX", "sencillo", "12A"),
        ("TAPO", "2025", "CDMX", "doble", "32"),
        ("SUR", "2026", "EDOMEX", "sencillo", "A"),
        ("ESPECIALES", "2026", "CDMX", "doble", "AA"),
    ):
        row = find(rows, base=sample[0], vigencia=sample[1], ambito=sample[2], tipo=sample[3], zona=sample[4])
        print(sample, fmt(row["precio"]), fmt(row["descuento"]), fmt(row["sitio"]), fmt(row["plata"]), "neto", fmt(row["neto"]))

    from collections import Counter

    counts = Counter((row["base"], row["vigencia"], row["ambito"], row["tipo"]) for row in rows)
    for key in sorted(counts):
        print(f"{key}: {counts[key]}")
    print("total", len(rows))
    print("rez agregadas", rez_added)
    renamed = [row["zona"] for row in rows if "·" in row["zona"]]
    print("renombradas", renamed)
    print("wrote", OUT)


if __name__ == "__main__":
    main()
