/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Asignatura;
import cl.usach.entities.Equipo;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.AsignaturaFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import java.util.Calendar;
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
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private AsignaturaFacadeLocal asignaturaFacade;

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
    
    private final int anoActual = Calendar.getInstance().get(Calendar.YEAR);
    
    public AsignaturaManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        asignaturas = asignaturaFacade.findAll();
    }

    public Integer getIdAsignatura() {
        return idAsignatura;
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
    
    public void nuevaAsignatura(){
        idUsuario = usuarioFacade.buscarPorLogin("fguzman");
        anoAsignatura = anoActual;
        cierreAsignatura = 0;
        Asignatura asignatura = new Asignatura(nombreAsignatura, creditoAsignatura, horasDeTrabajoAsignatura, semestreAsignatura, anoAsignatura, cierreAsignatura, idUsuario);
        
        FacesMessage msg = new FacesMessage("Asignatura Agregada",asignatura.getNombreAsignatura());  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        asignaturaFacade.create(asignatura);
        asignaturas = asignaturaFacade.findAll();
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
        asignaturas = asignaturaFacade.findAll();
    }
    
}
