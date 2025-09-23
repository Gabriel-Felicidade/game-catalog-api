package br.senac.tsi.gamecatalog;

import br.senac.tsi.gamecatalog.SearchJogoResponse;
import br.senac.tsi.gamecatalog.Desenvolvedora;
import br.senac.tsi.gamecatalog.Genero;
import br.senac.tsi.gamecatalog.Jogo;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/jogos")
public class JogoResource {

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        PanacheQuery<Jogo> query;
        if (q == null || q.isBlank()) {
            query = Jogo.findAll(sortObj);
        } else {
            try {
                int numero = Integer.parseInt(q);
                query = Jogo.find("anoLancamento = ?1", sortObj, numero);
            } catch (NumberFormatException e) {
                query = Jogo.find("lower(titulo) like ?1", sortObj, "%" + q.toLowerCase() + "%");
            }
        }

        List<Jogo> jogos = query.page(page, size).list();
        var response = new SearchJogoResponse();
        response.jogos = jogos;
        response.totalItens = query.count();
        response.totalPaginas = query.pageCount();
        response.temMais = page < (query.pageCount() - 1);
        response.proximaPagina = response.temMais ? "/jogos/search?q="+(q != null ? q : "")+"&page="+(page + 1)+"&size="+size : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") long id) {
        return Jogo.findByIdOptional(id)
                .map(jogo -> Response.ok(jogo).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    public Response insert(@Valid Jogo jogo) {
        jogo.id = null; // Garante que é uma nova entidade

        // Valida e resolve a desenvolvedora
        if (jogo.desenvolvedora == null || jogo.desenvolvedora.id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID da desenvolvedora é obrigatório.").build();
        }
        Desenvolvedora d = Desenvolvedora.findById(jogo.desenvolvedora.id);
        if (d == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Desenvolvedora não encontrada.").build();
        }
        jogo.desenvolvedora = d;

        // Valida e resolve os gêneros
        if (jogo.generos != null) {
            Set<Genero> resolved = new HashSet<>();
            for (Genero g : jogo.generos) {
                if (g == null || g.id == null) continue;
                Genero fetched = Genero.findById(g.id);
                if (fetched == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Gênero com id " + g.id + " não existe.").build();
                }
                resolved.add(fetched);
            }
            jogo.generos = resolved;
        }

        Jogo.persist(jogo);
        return Response.status(Response.Status.CREATED).entity(jogo).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Jogo newJogo) {
        Jogo entity = Jogo.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entity.titulo = newJogo.titulo;
        entity.sinopse = newJogo.sinopse;
        entity.anoLancamento = newJogo.anoLancamento;
        entity.notaCritica = newJogo.notaCritica;

        // Atualiza desenvolvedora
        if (newJogo.desenvolvedora != null && newJogo.desenvolvedora.id != null) {
            Desenvolvedora d = Desenvolvedora.findById(newJogo.desenvolvedora.id);
            if (d == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Desenvolvedora não encontrada.").build();
            }
            entity.desenvolvedora = d;
        } else {
            entity.desenvolvedora = null;
        }

        // Atualiza gêneros
        entity.generos.clear();
        if (newJogo.generos != null) {
            for (Genero g : newJogo.generos) {
                if (g != null && g.id != null) {
                    Genero fetched = Genero.findById(g.id);
                    if (fetched != null) {
                        entity.generos.add(fetched);
                    }
                }
            }
        }

        return Response.ok(entity).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Jogo entity = Jogo.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entity.delete();
        return Response.noContent().build();
    }
}

