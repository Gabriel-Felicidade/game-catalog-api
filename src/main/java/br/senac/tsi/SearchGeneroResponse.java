package br.senac.tsi.gamecatalog;

import br.senac.tsi.gamecatalog.Genero;
import java.util.ArrayList;
import java.util.List;

public class SearchGeneroResponse {
    public List<Genero> generos = new ArrayList<>();
    public long totalItens;
    public int totalPaginas;
    public boolean temMais;
    public String proximaPagina;
}

