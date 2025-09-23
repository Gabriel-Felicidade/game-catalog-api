package br.senac.tsi.gamecatalog;

import br.senac.tsi.gamecatalog.SearchGeneroResponse;
import br.senac.tsi.gamecatalog.Genero;
import br.senac.tsi.gamecatalog.Jogo;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Set;

@Path("/generos")
public class GeneroResource {

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        PanacheQuery<Genero> query = (q == null || q.isBlank())
                ? Genero.findAll(sortObj)
                : Genero.find("lower(nome) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<Genero> generos = query.page(page, size).list();
        var response = new SearchGeneroResponse();
        response.generos = generos;
        response.totalItens = query.count();
        response.totalPaginas = query.pageCount();
        response.temMais = page < (query.pageCount() - 1);
        response.proximaPagina = response.temMais ? "/generos/search?q="+(q != null ? q : "")+"&page="+(page + 1)+"&size="+size : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") long id) {
        return Genero.findByIdOptional(id)
                .map(genero -> Response.ok(genero).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    public Response insert(@Valid Genero genero) {
        genero.id = null; // Garante que é uma nova entidade
        Genero.persist(genero);
        return Response.status(Response.Status.CREATED).entity(genero).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Genero newGenero) {
        Genero entity = Genero.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entity.nome = newGenero.nome;
        entity.descricao = newGenero.descricao;
        return Response.ok(entity).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Genero entity = Genero.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (Jogo.count("?1 MEMBER OF generos", entity) > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Não é possível deletar. Gênero possui jogos vinculados.")
                    .build();
        }
        entity.delete();
        return Response.noContent().build();
    }
}

