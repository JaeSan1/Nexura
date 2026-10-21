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
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {

    // EVENTOS
    @GET("eventos?select=*&order=created_at.desc")
    Call<List<Evento>> obtenerEventos();

    @GET("eventos?select=*&order=contador_likes.desc&limit=3")
    Call<List<Evento>> obtenerEventosDestacados();

    @GET("eventos?select=*")
    Call<List<Evento>> buscarEventosPorTitulo(@Query("titulo") String filtroTitulo);

    @GET("eventos?select=*")
    Call<List<Evento>> obtenerEventosPorCiudad(@Query("ciudad") String filtroCiudad);

    @POST("eventos")
    Call<Void> crearEvento(@Body Evento evento);

    // SUBIDA DE ARCHIVOS A SUPABASE STORAGE
    @POST("storage/v1/object/eventos-imagenes/{nombre_archivo}")
    Call<Void> subirImagenBucket(
            @Header("Content-Type") String contentType,
            @Path("nombre_archivo") String nombreArchivo,
            @Body RequestBody bytesImagen
    );

    // AUTENTICACIÓN Y PERFIL
    @POST("perfiles")
    Call<Void> registrarUsuario(@Body Usuario nuevoUsuario);

    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorGamertag(@Query("gamertag") String filtroGamertag);

    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorId(@Query("id") String filtroId);

    @GET("perfiles?select=*")
    Call<List<Usuario>> verificarCredenciales(@Query("correo") String filtroCorreo);

    @PATCH("perfiles")
    Call<Void> actualizarPerfil(
            @Query("id") String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @PATCH("perfiles")
    Call<Void> actualizarXpUsuario(
            @Query("id") String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    @PATCH("perfiles")
    Call<Void> actualizarSeguidoresUsuario(
            @Query("id") String filtroId,
            @Body Map<String, Object> camposActualizados
    );

    // ELIMINACIÓN DE PERFIL
    @DELETE("perfiles")
    Call<Void> eliminarUsuario(@Query("id") String filtroId);

    // ASISTENCIAS Y FAVORITOS (MIS EVENTOS)
    @POST("asistencias")
    Call<Void> agregarAsistencia(@Body Map<String, Object> datosAsistencia);

    @GET("asistencias?select=*,eventos(*)")
    Call<List<Map<String, Object>>> obtenerAsistenciasUsuario(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("asistio") String filtroAsistio
    );

    @DELETE("asistencias")
    Call<Void> eliminarAsistencia(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId
    );

    @PATCH("asistencias")
    Call<Void> validarAsistenciaGps(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId,
            @Body Map<String, Object> camposActualizados
    );

    // COMENTARIOS
    @GET("comentarios?select=*&order=created_at.desc")
    Call<List<Comentario>> obtenerComentariosPorEvento(@Query("evento_id") String filtroEventoId);

    @POST("comentarios")
    Call<Void> publicarComentario(@Body Comentario comentario);

    // NOTIFICACIONES
    @GET("notificaciones?select=*&order=created_at.desc")
    Call<List<Notificacion>> obtenerNotificaciones();

    @POST("notificaciones")
    Call<Void> emitirAvisoOrganizador(@Body Notificacion nuevaNotificacion);

    // LOGROS
    @GET("logros?select=*")
    Call<List<Logro>> obtenerLogros();
}