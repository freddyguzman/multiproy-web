/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Asignatura;
import cl.usach.entities.Equipo;
import cl.usach.entities.RolUsuario;
import cl.usach.entities.SprintAsignatura;
import cl.usach.entities.SprintGrupos;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.AsignaturaFacadeLocal;
import cl.usach.sessionbeans.RolUsuarioFacadeLocal;
import cl.usach.sessionbeans.SprintAsignaturaFacadeLocal;
import cl.usach.sessionbeans.SprintGruposFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author FGT
 */
@Named(value = "asignaturaManagedBean")
@ViewScoped
public class AsignaturaManagedBean {
    @EJB
    private RolUsuarioFacadeLocal rolUsuarioFacade;
    @EJB
    private SprintGruposFacadeLocal sprintGruposFacade;
    @EJB
    private SprintAsignaturaFacadeLocal sprintAsignaturaFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private AsignaturaFacadeLocal asignaturaFacade;
    
    private final SesionManagedBean sesionManagedBean = new SesionManagedBean();

    private Integer idAsignatura;
    private String nombreAsignatura;
    private int creditoAsignatura;
    private int horasDeTrabajoAsignatura;
    private int semestreAsignatura;
    private int anoAsignatura;
    private int cierreAsignatura;
    private Usuario idUsuario;
    private Equipo idEquipo;
    private List<Asignatura> asignaturas;
    
    private Asignatura asignaturaSeleccionada;
    
    private List<Asignatura> asignaturasFiltradas;
    
    private List<SprintAsignatura> sprintsAsignatura;
    private int idSprintAsignatura;
    private String nombreSprintAsignatura;
    private String descripcionSprintAsignatura;
    private Date fechaInicioSprintAsignatura;
    private Date fechaTerminoSprintAsignatura;
    private SprintAsignatura sprintAsignaturaSeleccionado;
    
    private int idSprintGrupo;
    private String nombreSprintGrupo;
    private String objetivoTecnicoSprintGrupo;
    private String objetivoUsuarioSprintGrupo;
    private Usuario idUsuarioSprintGrupo;
    private SprintGrupos sprintGrupoSeleccionado;
    
    private List<Usuario> usuariosAlumnos;
    private Usuario usuarioAlumnoSeleccionado;
    
    private final int anoActual = Calendar.getInstance().get(Calendar.YEAR);
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    
    public AsignaturaManagedBean() {
    }
    
    @PostConstruct
    public void init(){
       actualizarLista();
       buscarUsuariosAlumnos();
    }

    public Integer getIdAsignatura() {
        return idAsignatura;
    }
    
    public void nuevaAsignatura(){
        idUsuario = usuarioFacade.buscarPorLogin(loginUsuario);
        anoAsignatura = anoActual;
        cierreAsignatura = 0;
        Asignatura asignatura = new Asignatura(nombreAsignatura, creditoAsignatura, horasDeTrabajoAsignatura, semestreAsignatura, anoAsignatura, cierreAsignatura, idUsuario);
        
        FacesMessage msg = new FacesMessage("Asignatura Agregada",asignatura.getNombreAsignatura());  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        asignaturaFacade.create(asignatura);
        actualizarLista();
    }
    
    public void onEdit(RowEditEvent event){
        Asignatura asignatura = (Asignatura) event.getObject();
        asignaturaFacade.edit(asignatura);
        FacesMessage msg = new FacesMessage("Asignatura Editada","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onCancel(RowEditEvent event){
        FacesMessage msg = new FacesMessage("No se realizó ningun cambio","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void eliminarAsignatura(){
        FacesMessage msg = new FacesMessage("Asignatura Eliminada","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        asignaturaFacade.remove(asignaturaSeleccionada);
        actualizarLista();
    }
    
    public void actualizarLista(){
        if(sesionManagedBean.checkUserAdmin()){
            asignaturas = asignaturaFacade.findAll();
        }else{
            Usuario usuarioActual = usuarioFacade.buscarPorLogin(loginUsuario);
            asignaturas = asignaturaFacade.buscarPorIdUsuario(usuarioActual);
        }
    }
    
    public void buscarSprints(){
        sprintsAsignatura = sprintAsignaturaFacade.buscarPorAsignatura(asignaturaSeleccionada);
    }
    
    public void nuevoSprintAsignaturas(Asignatura asig){
        if(nombreSprintAsignatura != null && fechaInicioSprintAsignatura != null && fechaTerminoSprintAsignatura != null){
            SprintAsignatura sprintA = new SprintAsignatura(nombreSprintAsignatura, 
                descripcionSprintAsignatura, fechaInicioSprintAsignatura, 
                fechaTerminoSprintAsignatura, asig);
            sprintAsignaturaFacade.create(sprintA);
            FacesMessage msg = new FacesMessage("Sprint Agregado",nombreSprintAsignatura);  
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            limpiarDatos();
        }else{
            FacesMessage msg = new FacesMessage("Ingrese datos validos");  
            FacesContext.getCurrentInstance().addMessage(null, msg); 
        }
        
        buscarSprints();
    }
    
    public void editarSprintAsignatura(){
        sprintAsignaturaFacade.edit(sprintAsignaturaSeleccionado);
        FacesMessage msg = new FacesMessage("Sprint Editado","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        limpiarDatos();
    }
    
    public void eliminarSprintAsignatura(SprintAsignatura sprintA){
        sprintAsignaturaFacade.remove(sprintA);
        FacesMessage msg = new FacesMessage("Sprint Asignatura Eliminada","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        buscarSprints();
    }
    
    public void buscarDatosEditarSprintAsigntarua(){
        System.out.println("asadsd " + sprintAsignaturaSeleccionado);
    }
    
    public List<SprintGrupos> buscarSprintGrupos(SprintAsignatura sprintA){
        List<SprintGrupos> sprintsG = sprintGruposFacade.buscarPorSprintAsignatura(sprintA);
        return sprintsG;
    }
    
    public void buscarUsuariosAlumnos(){
        RolUsuario ru = rolUsuarioFacade.buscarPorNombre("Alumno");
        usuariosAlumnos = usuarioFacade.buscarPorIdRolUsuario(ru);
    }
    
    public void nuevoSprintGrupo(SprintAsignatura sprintA){
        SprintGrupos springG = new SprintGrupos(nombreSprintGrupo,
             objetivoTecnicoSprintGrupo, objetivoUsuarioSprintGrupo, usuarioAlumnoSeleccionado, sprintA);
        
        sprintGruposFacade.create(springG);
        FacesMessage msg = new FacesMessage("Sprint Grupo Agregado", springG.getNombreSprintGrupo());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        limpiarDatos();
    }
    
    public void limpiarDatos(){
        idSprintAsignatura = 0;
        nombreSprintAsignatura  = null;
        descripcionSprintAsignatura = null;
        fechaInicioSprintAsignatura = null;
        fechaTerminoSprintAsignatura = null;
        sprintAsignaturaSeleccionado = null;

        idSprintGrupo = 0;
        nombreSprintGrupo = null;
        objetivoTecnicoSprintGrupo = null;
        objetivoUsuarioSprintGrupo = null;
        idUsuarioSprintGrupo = null;
    }
    
    public void eliminarSprintGrupo(){
        sprintGruposFacade.remove(sprintGrupoSeleccionado);
        FacesMessage msg = new FacesMessage("Sprint Grupo Eliminado", sprintGrupoSeleccionado.getNombreSprintGrupo());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void editarSprintGrupo(){
        sprintGruposFacade.edit(sprintGrupoSeleccionado);
        FacesMessage msg = new FacesMessage("Sprint Grupo Editado",sprintGrupoSeleccionado.getNombreSprintGrupo());  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        limpiarDatos();
    }
    
    public String formatoFecha(Date fecha){
        if(fecha == null) return "-";
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaF = df.format(fecha);
        return fechaF;
    }

    public void setIdAsignatura(Integer idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
    }

    public int getCreditoAsignatura() {
        return creditoAsignatura;
    }

    public void setCreditoAsignatura(int creditoAsignatura) {
        this.creditoAsignatura = creditoAsignatura;
    }

    public int getHorasDeTrabajoAsignatura() {
        return horasDeTrabajoAsignatura;
    }

    public void setHorasDeTrabajoAsignatura(int horasDeTrabajoAsignatura) {
        this.horasDeTrabajoAsignatura = horasDeTrabajoAsignatura;
    }

    public int getSemestreAsignatura() {
        return semestreAsignatura;
    }

    public void setSemestreAsignatura(int semestreAsignatura) {
        this.semestreAsignatura = semestreAsignatura;
    }

    public int getAnoAsignatura() {
        return anoAsignatura;
    }

    public void setAnoAsignatura(int anoAsignatura) {
        this.anoAsignatura = anoAsignatura;
    }

    public int getCierreAsignatura() {
        return cierreAsignatura;
    }

    public void setCierreAsignatura(int cierreAsignatura) {
        this.cierreAsignatura = cierreAsignatura;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Equipo getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Equipo idEquipo) {
        this.idEquipo = idEquipo;
    }

    public List<Asignatura> getAsignaturas() {
        return asignaturas;
    }

    public void setAsignaturas(List<Asignatura> asignaturas) {
        this.asignaturas = asignaturas;
    }

    public List<Asignatura> getAsignaturasFiltradas() {
        return asignaturasFiltradas;
    }

    public void setAsignaturasFiltradas(List<Asignatura> asignaturasFiltradas) {
        this.asignaturasFiltradas = asignaturasFiltradas;
    }

    public Asignatura getAsignaturaSeleccionada() {
        return asignaturaSeleccionada;
    }

    public void setAsignaturaSeleccionada(Asignatura asignaturaSeleccionada) {
        this.asignaturaSeleccionada = asignaturaSeleccionada;
    }   

    public int getAnoActual() {
        return anoActual;
    }

    public List<SprintAsignatura> getSprintsAsignatura() {
        return sprintsAsignatura;
    }

    public void setSprintsAsignatura(List<SprintAsignatura> sprintsAsignatura) {
        this.sprintsAsignatura = sprintsAsignatura;
    }

    public int getIdSprintAsignatura() {
        return idSprintAsignatura;
    }

    public void setIdSprintAsignatura(int idSprintAsignatura) {
        this.idSprintAsignatura = idSprintAsignatura;
    }

    public String getNombreSprintAsignatura() {
        return nombreSprintAsignatura;
    }

    public void setNombreSprintAsignatura(String nombreSprintAsignatura) {
        this.nombreSprintAsignatura = nombreSprintAsignatura;
    }

    public String getDescripcionSprintAsignatura() {
        return descripcionSprintAsignatura;
    }

    public void setDescripcionSprintAsignatura(String descripcionSprintAsignatura) {
        this.descripcionSprintAsignatura = descripcionSprintAsignatura;
    }

    public Date getFechaInicioSprintAsignatura() {
        return fechaInicioSprintAsignatura;
    }

    public void setFechaInicioSprintAsignatura(Date fechaInicioSprintAsignatura) {
        this.fechaInicioSprintAsignatura = fechaInicioSprintAsignatura;
    }

    public Date getFechaTerminoSprintAsignatura() {
        return fechaTerminoSprintAsignatura;
    }

    public void setFechaTerminoSprintAsignatura(Date fechaTerminoSprintAsignatura) {
        this.fechaTerminoSprintAsignatura = fechaTerminoSprintAsignatura;
    }

    public int getIdSprintGrupo() {
        return idSprintGrupo;
    }

    public void setIdSprintGrupo(int idSprintGrupo) {
        this.idSprintGrupo = idSprintGrupo;
    }

    public String getNombreSprintGrupo() {
        return nombreSprintGrupo;
    }

    public void setNombreSprintGrupo(String nombreSprintGrupo) {
        this.nombreSprintGrupo = nombreSprintGrupo;
    }

    public String getObjetivoTecnicoSprintGrupo() {
        return objetivoTecnicoSprintGrupo;
    }

    public void setObjetivoTecnicoSprintGrupo(String objetivoTecnicoSprintGrupo) {
        this.objetivoTecnicoSprintGrupo = objetivoTecnicoSprintGrupo;
    }

    public String getObjetivoUsuarioSprintGrupo() {
        return objetivoUsuarioSprintGrupo;
    }

    public void setObjetivoUsuarioSprintGrupo(String objetivoUsuarioSprintGrupo) {
        this.objetivoUsuarioSprintGrupo = objetivoUsuarioSprintGrupo;
    }

    public Usuario getIdUsuarioSprintGrupo() {
        return idUsuarioSprintGrupo;
    }

    public void setIdUsuarioSprintGrupo(Usuario idUsuarioSprintGrupo) {
        this.idUsuarioSprintGrupo = idUsuarioSprintGrupo;
    }    

    public SprintAsignatura getSprintAsignaturaSeleccionado() {
        return sprintAsignaturaSeleccionado;
    }

    public void setSprintAsignaturaSeleccionado(SprintAsignatura sprintAsignaturaSeleccionado) {
        this.sprintAsignaturaSeleccionado = sprintAsignaturaSeleccionado;
    }

    public List<Usuario> getUsuariosAlumnos() {
        return usuariosAlumnos;
    }

    public void setUsuariosAlumnos(List<Usuario> usuariosAlumnos) {
        this.usuariosAlumnos = usuariosAlumnos;
    }

    public Usuario getUsuarioAlumnoSeleccionado() {
        return usuarioAlumnoSeleccionado;
    }

    public void setUsuarioAlumnoSeleccionado(Usuario usuarioAlumnoSeleccionado) {
        this.usuarioAlumnoSeleccionado = usuarioAlumnoSeleccionado;
    }

    public SprintGrupos getSprintGrupoSeleccionado() {
        return sprintGrupoSeleccionado;
    }

    public void setSprintGrupoSeleccionado(SprintGrupos sprintGrupoSeleccionado) {
        this.sprintGrupoSeleccionado = sprintGrupoSeleccionado;
    }
    
}
