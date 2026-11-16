package com.example.nexura.network;

import com.example.nexura.model.Comentario;
import com.example.nexura.model.Evento;
import com.example.nexura.model.Logro;
import com.example.nexura.model.Notificacion;
import com.example.nexura.model.Usuario;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {

    // --- EVENTOS ---
    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)&order=created_at.desc")
    Call<List<Evento>> obtenerEventos();

    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)&order=contador_likes.desc&limit=3")
    Call<List<Evento>> obtenerEventosDestacados();

    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)")
    Call<List<Evento>> buscarEventosPorTitulo(@Query("titulo") String filtroTitulo);

    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)")
    Call<List<Evento>> obtenerEventosPorCiudad(@Query("ciudad") String filtroCiudad);

    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)&order=created_at.desc")
    Call<List<Evento>> obtenerEventosPorOrganizador(
            @Query(value = "organizador_id", encoded = true) String filtroOrganizadorId
    );

    @GET("eventos?select=*,perfiles(foto_perfil_url,gamertag)")
    Call<List<Evento>> obtenerEventosPorCategoria(@Query(value = "categoria", encoded = true) String filtroCategoria);

    @POST("eventos")
    Call<Void> crearEvento(@Body Map<String, Object> bodyEvento);

    @PATCH("eventos")
    Call<Void> actualizarEvento(
            @Query(value = "id", encoded = true) String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @DELETE("eventos")
    Call<Void> eliminarEvento(
            @Query(value = "id", encoded = true) String filtroId,
            @Query(value = "organizador_id", encoded = true) String filtroOrganizadorId
    );

    // --- STORAGE ---
    @Headers("x-upsert: true")
    @POST("/storage/v1/object/eventos-imagenes/{nombre_archivo}")
    Call<Void> subirImagenBucket(
            @Header("Content-Type") String contentType,
            @Path("nombre_archivo") String nombreArchivo,
            @Body RequestBody bytesImagen
    );

    // --- AUTENTICACIÓN Y PERFILES ---
    @Headers("Prefer: return=representation")
    @POST("perfiles")
    Call<List<Usuario>> registrarUsuario(@Body Usuario nuevoUsuario);

    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorGamertag(@Query(value = "gamertag", encoded = true) String filtroGamertag);

    @GET("perfiles?select=*")
    Call<List<Usuario>> verificarCredenciales(@Query(value = "correo", encoded = true) String filtroCorreo);

    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorId(@Query(value = "id", encoded = true) String filtroId);

    @PATCH("perfiles")
    Call<Void> actualizarPerfil(
            @Query(value = "id", encoded = true) String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @PATCH("perfiles")
    Call<Void> actualizarXpUsuario(
            @Query(value = "id", encoded = true) String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @PATCH("perfiles")
    Call<Void> actualizarSeguidoresUsuario(
            @Query(value = "id", encoded = true) String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @DELETE("perfiles")
    Call<Void> eliminarUsuario(@Query(value = "id", encoded = true) String filtroId);

    // --- LIKES DE PERFIL Y REPUTACIÓN ---
    @POST("perfil_likes")
    Call<Void> darLikePerfil(@Body Map<String, Object> body);

    @DELETE("perfil_likes")
    Call<Void> quitarLikePerfil(
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId,
            @Query(value = "perfil_destinatario_id", encoded = true) String filtroDestinatarioId
    );

    @GET("perfil_likes?select=*")
    Call<List<Object>> verificarLikePerfil(
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId,
            @Query(value = "perfil_destinatario_id", encoded = true) String filtroDestinatarioId
    );

    // --- ASISTENCIAS Y FAVORITOS ---
    @Headers("Prefer: resolution=merge-duplicates")
    @POST("asistencias")
    Call<Void> agregarAsistencia(@Body Map<String, Object> datosAsistencia);

    @GET("asistencias?select=*,eventos(*,perfiles(foto_perfil_url,gamertag))")
    Call<List<Object>> obtenerAsistenciasUsuario(
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId
    );

    @DELETE("asistencias")
    Call<Void> eliminarAsistencia(
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId,
            @Query(value = "evento_id", encoded = true) String filtroEventoId
    );

    @PATCH("asistencias")
    Call<Void> validarAsistenciaGps(
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId,
            @Query(value = "evento_id", encoded = true) String filtroEventoId,
            @Body Map<String, Object> camposActualizados
    );

    // --- COMENTARIOS ---
    @GET("comentarios?select=*&order=created_at.desc")
    Call<List<Comentario>> obtenerComentariosPorEvento(@Query(value = "evento_id", encoded = true) String filtroEventoId);

    @POST("comentarios")
    Call<Void> publicarComentario(@Body Comentario comentario);

    @DELETE("comentarios")
    Call<Void> eliminarComentario(
            @Query(value = "id", encoded = true) String filtroComentarioId,
            @Query(value = "usuario_id", encoded = true) String filtroUsuarioId
    );

    // --- NOTIFICACIONES ---
    @GET("notificaciones?select=*&order=created_at.desc")
    Call<List<Notificacion>> obtenerNotificaciones();

    @POST("notificaciones")
    Call<Void> emitirAvisoOrganizador(@Body Notificacion nuevaNotificacion);

    // --- LOGROS ---
    @GET("logros?select=*")
    Call<List<Logro>> obtenerLogros();

    // --- SEGUIDORES ---
    @POST("seguidores")
    Call<Void> seguirUsuario(@Body Map<String, Object> body);

    @DELETE("seguidores")
    Call<Void> dejarDeSeguirUsuario(
            @Query(value = "seguidor_id", encoded = true) String filtroSeguidorId,
            @Query(value = "seguido_id", encoded = true) String filtroSeguidoId
    );

    @GET("seguidores?select=*")
    Call<List<Object>> verificarSeguimiento(
            @Query(value = "seguidor_id", encoded = true) String filtroSeguidorId,
            @Query(value = "seguido_id", encoded = true) String filtroSeguidoId
    );

    @GET("notificaciones?select=*&order=created_at.desc")
    Call<List<Notificacion>> obtenerNotificacionesPorUsuario(
            @Query(value = "usuario_destino_id", encoded = true) String filtroUsuarioDestino
    );
}