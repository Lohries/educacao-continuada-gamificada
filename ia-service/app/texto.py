"""Normalizacao de texto em portugues compartilhada pela recomendacao e pelo assistente."""
import re
import unicodedata

STOPWORDS = {
    "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das", "em", "no", "na",
    "nos", "nas", "por", "para", "com", "sem", "e", "ou", "que", "como", "qual", "quais", "se", "ao",
    "aos", "eu", "meu", "minha", "sobre", "mais", "menos", "ja", "ser", "ter", "tem", "sao", "esta",
    "isso", "esse", "essa", "ele", "ela", "voce", "posso", "quero",
}


def tokens(texto: str) -> list[str]:
    """Minusculas, sem acento, sem pontuacao e sem stopwords."""
    sem_acento = unicodedata.normalize("NFKD", texto or "").encode("ascii", "ignore").decode()
    palavras = re.findall(r"[a-z0-9]+", sem_acento.lower())
    return [p for p in palavras if len(p) > 1 and p not in STOPWORDS]
