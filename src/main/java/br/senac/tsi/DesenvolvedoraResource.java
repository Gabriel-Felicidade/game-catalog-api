package br.senac.tsi.gamecatalog;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Set;

@Path("/desenvolvedoras")
public class DesenvolvedoraResource {

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        // --- CORREÇÃO: Validar o campo de ordenação ---
        Set<String> allowedSortFields = Set.of("id", "nome", "fundacao", "nacionalidade");
        if (!allowedSortFields.contains(sort)) {
            sort = "id"; // Volta para o padrão se o campo for inválido
        }

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        PanacheQuery<Desenvolvedora> query = (q == null || q.isBlank())
                ? Desenvolvedora.findAll(sortObj)
                : Desenvolvedora.find("lower(nome) like ?1 or lower(nacionalidade) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<Desenvolvedora> desenvolvedoras = query.page(page, size).list();
        var response = new SearchDesenvolvedoraResponse();
        response.desenvolvedoras = desenvolvedoras;
        response.totalItens = query.count();
        response.totalPaginas = query.pageCount();
        response.temMais = page < (query.pageCount() - 1);

        // --- MELHORIA: Corrigido para incluir sort e direction na URL ---
        response.proximaPagina = response.temMais
                ? String.format("/desenvolvedoras/search?q=%s&sort=%s&direction=%s&page=%d&size=%d",
                (q != null ? q : ""), sort, direction, (page + 1), size)
                : "";

        return Response.ok(response).build();
    }

    // ... (o restante da classe permanece o mesmo) ...
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") long id) {
        return Desenvolvedora.findByIdOptional(id)
                .map(desenvolvedora -> Response.ok(desenvolvedora).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    public Response insert(@Valid Desenvolvedora dev) {
        dev.id = null; // Garante que é uma nova entidade
        Desenvolvedora.persist(dev);
        return Response.status(Response.Status.CREATED).entity(dev).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Desenvolvedora newDev) {
        Desenvolvedora entity = Desenvolvedora.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entity.nome = newDev.nome;
        entity.fundacao = newDev.fundacao;
        entity.nacionalidade = newDev.nacionalidade;

        if (newDev.perfil != null) {
            if (entity.perfil == null) {
                entity.perfil = new PerfilDesenvolvedora();
            }
            entity.perfil.historia = newDev.perfil.historia;
            entity.perfil.principaisFranquias = newDev.perfil.principaisFranquias;
        } else {
            entity.perfil = null;
        }
        return Response.ok(entity).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") long id) {
        Desenvolvedora entity = Desenvolvedora.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (Jogo.count("desenvolvedora.id", id) > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Não é possível deletar. Desenvolvedora possui jogos vinculados.")
                    .build();
        }
        entity.delete();
        return Response.noContent().build();
    }
}