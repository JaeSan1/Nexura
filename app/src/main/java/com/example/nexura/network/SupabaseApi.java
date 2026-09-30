package com.example.nexura.network;

import com.example.nexura.model.Comentario;
import com.example.nexura.model.Evento;
import com.example.nexura.model.Logro;
import com.example.nexura.model.Notificacion;
import com.example.nexura.model.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {


    // Listar los eventos
    @GET("eventos?select=*&order=created_at.desc")
    Call<List<Evento>> obtenerEventos();

    // 3 eventos con más likes
    @GET("eventos?select=*&order=contador_likes.desc&limit=3")
    Call<List<Evento>> obtenerEventosDestacados();

    // Filtro por título
    @GET("eventos?select=*")
    Call<List<Evento>> buscarEventosPorTitulo(@Query("titulo") String filtroTitulo);

    // Filtro por ciudad
    @GET("eventos?select=*")
    Call<List<Evento>> obtenerEventosPorCiudad(@Query("ciudad") String filtroCiudad);

    @POST("eventos")
    Call<Void> crearEvento(@Body Evento evento);

    // PERFILES Y AUTENTICACIÓN
    @POST("perfiles")
    Call<Void> registrarUsuario(@Body Usuario nuevoUsuario);

    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorGamertag(@Query("gamertag") String filtroGamertag);

    // Verificar si el correo ya existe en Login
    @GET("perfiles?select=*")
    Call<List<Usuario>> verificarCredenciales(@Query("correo") String filtroCorreo);

    // Actualizar campos
    @PATCH("perfiles")
    Call<Void> actualizarPerfil(
            @Query("gamertag") String filtroGamertag,
            @Body Map<String, Object> camposActualizados
    );

    // 3. ASISTENCIAS Y FAVORITOS
    @POST("asistencias")
    Call<Void> agregarAsistencia(@Body Map<String, Object> datosAsistencia);

    // Eliminar de "Mis Eventos"
    @DELETE("asistencias")
    Call<Void> eliminarAsistencia(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId
    );

    // Validar GPS
    @PATCH("asistencias")
    Call<Void> validarAsistenciaGps(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId,
            @Body Map<String, Object> camposActualizados
    );

    // COMENTARIOS
    @GET("comentarios?select=*&order=created_at.desc")
    Call<List<Comentario>> obtenerComentariosPorEvento(@Query("evento_id") String filtroEventoId);

    // Publicar nuevo comentario
    @POST("comentarios")
    Call<Void> publicarComentario(@Body Comentario comentario);

    // NOTIFICACIONES
    @GET("notificaciones?select=*&order=created_at.desc")
    Call<List<Notificacion>> obtenerNotificaciones();

    // aviso urgente
    @POST("notificaciones")
    Call<Void> emitirAvisoOrganizador(@Body Notificacion nuevaNotificacion);

    // GAMIFICACIÓN
    @GET("logros?select=*")
    Call<List<Logro>> obtenerLogros();
}