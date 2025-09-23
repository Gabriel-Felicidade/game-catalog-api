package br.senac.tsi.gamecatalog;

import br.senac.tsi.gamecatalog.Jogo;
import java.util.ArrayList;
import java.util.List;

public class SearchJogoResponse {
    public List<Jogo> jogos = new ArrayList<>();
    public long totalItens;
    public int totalPaginas;
    public boolean temMais;
    public String proximaPagina;
}

