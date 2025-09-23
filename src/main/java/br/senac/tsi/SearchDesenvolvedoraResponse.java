package br.senac.tsi.gamecatalog;

import br.senac.tsi.gamecatalog.Desenvolvedora;
import java.util.ArrayList;
import java.util.List;

public class SearchDesenvolvedoraResponse {
    public List<Desenvolvedora> desenvolvedoras = new ArrayList<>();
    public long totalItens;
    public int totalPaginas;
    public boolean temMais;
    public String proximaPagina;
}

