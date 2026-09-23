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

    // Guardar nuevo evento en la base de datos
    @POST("eventos")
    Call<Void> crearEvento(@Body Evento evento);

    // ==========================================
    // 2. PERFILES Y AUTENTICACIÓN (Login, Registro, Perfil, EditarPerfil)
    // ==========================================

    // Registrar nuevo usuario en la tabla perfiles
    @POST("perfiles")
    Call<Void> registrarUsuario(@Body Usuario nuevoUsuario);

    // Consultar perfil por gamertag (ej. "eq.TheGoat99")
    @GET("perfiles?select=*")
    Call<List<Usuario>> obtenerPerfilPorGamertag(@Query("gamertag") String filtroGamertag);

    // Verificar si el correo ya existe en Login / Registro
    @GET("perfiles?select=*")
    Call<List<Usuario>> verificarCredenciales(@Query("correo") String filtroCorreo);

    // Actualizar campos parciales (biografía, ciudad, gamertag, título equipado)
    @PATCH("perfiles")
    Call<Void> actualizarPerfil(
            @Query("gamertag") String filtroGamertag,
            @Body Map<String, Object> camposActualizados
    );

    // ==========================================
    // 3. ASISTENCIAS Y FAVORITOS (MisEventos, Detalle_Evento)
    // ==========================================

    // Guardar evento en la lista de favoritos/asistencias
    @POST("asistencias")
    Call<Void> agregarAsistencia(@Body Map<String, Object> datosAsistencia);

    // Eliminar de "Mis Eventos"
    @DELETE("asistencias")
    Call<Void> eliminarAsistencia(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId
    );

    // Validar GPS para activar el trigger de subida de nivel y XP
    @PATCH("asistencias")
    Call<Void> validarAsistenciaGps(
            @Query("usuario_id") String filtroUsuarioId,
            @Query("evento_id") String filtroEventoId,
            @Body Map<String, Object> camposActualizados
    );

    // ==========================================
    // 4. COMENTARIOS (Detalle_Evento)
    // ==========================================

    // Listar comentarios asociados a un evento específico
    @GET("comentarios?select=*&order=created_at.desc")
    Call<List<Comentario>> obtenerComentariosPorEvento(@Query("evento_id") String filtroEventoId);

    // Publicar nuevo comentario
    @POST("comentarios")
    Call<Void> publicarComentario(@Body Comentario comentario);

    // ==========================================
    // 5. NOTIFICACIONES (Notificaciones, PanelOrganizador)
    // ==========================================

    // Listar notificaciones y avisos de la comunidad
    @GET("notificaciones?select=*&order=created_at.desc")
    Call<List<Notificacion>> obtenerNotificaciones();

    // Difundir un aviso urgente desde el panel de organizador
    @POST("notificaciones")
    Call<Void> emitirAvisoOrganizador(@Body Notificacion nuevaNotificacion);

    // ==========================================
    // 6. GAMIFICACIÓN (Catálogo de Logros)
    // ==========================================

    // Listar las medallas e insignias disponibles en el sistema
    @GET("logros?select=*")
    Call<List<Logro>> obtenerLogros();
}