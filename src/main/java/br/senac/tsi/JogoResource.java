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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Link;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/jogos")
@Tag(name = "Jogos", description = "Operações relacionadas a jogos")
public class JogoResource {

    @Context
    UriInfo uriInfo; // Injete UriInfo para construir URLs

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        // Sugestão: Adicionar validação de sort aqui também
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

        // --- MELHORIA: Corrigido para incluir sort e direction na URL ---
        response.proximaPagina = response.temMais
                ? String.format("/jogos/search?q=%s&sort=%s&direction=%s&page=%d&size=%d",
                (q != null ? q : ""), sort, direction, (page + 1), size)
                : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar Jogo por ID", description = "Retorna os detalhes de um jogo específico.")
    @APIResponse(responseCode = "200", description = "Jogo encontrado")
    @APIResponse(responseCode = "404", description = "Jogo não encontrado")
    public Response getById(@PathParam("id") long id) {
        return Jogo.findByIdOptional(id)
                .map(jogo -> {
                    // Cria o link "self" (para o próprio recurso)
                    Link selfLink = Link.fromUri(uriInfo.getAbsolutePath()).rel("self").type("application/json").build();

                    // Cria o link para a desenvolvedora
                    Link devLink = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DesenvolvedoraResource.class).path(Long.toString(jogo.desenvolvedora.id))).rel("desenvolvedora").type("application/json").build();

                    // Adiciona os links na resposta
                    return Response.ok(jogo).links(selfLink, devLink).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    @Operation(summary = "Inserir novo Jogo", description = "Cria um novo jogo no catálogo.")
    @APIResponse(responseCode = "201", description = "Jogo criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos para o jogo")
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

