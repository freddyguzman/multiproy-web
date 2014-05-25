/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Cuenta;
import cl.usach.entities.Equipo;
import cl.usach.entities.TipoCuenta;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.CuentaFacadeLocal;
import cl.usach.sessionbeans.TipoCuentaFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import cl.usach.kanbanizesessionbeans.TableroKanbanizeLocal;
import cl.usach.trellosessionbeans.TableroTrelloLocal;
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
@Named(value = "cuentaManagedBean")
@ViewScoped
public class CuentaManagedBean {
    @EJB
    private TableroKanbanizeLocal tableroKanbanize;
    @EJB
    private TableroTrelloLocal tableroTrello;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;
    @EJB
    private TipoCuentaFacadeLocal tipoCuentaFacade;
    @EJB
    private CuentaFacadeLocal cuentaFacade;

    private Integer idCuenta;
    private String nombreUsuarioCuenta;
    private String emailCuenta;
    private String keyCuenta;
    private String secretCuenta;
    private String tokenCuenta;
    private List<Equipo> equipoList;
    private TipoCuenta idTipoCuenta;
    private Usuario idUsuario;
    
    private List<Cuenta> cuentas;
    private List<Cuenta> cuentasFiltradas;
    private List<TipoCuenta> tiposCuenta;
    
    private Cuenta cuentaSeleccionada;
    private TipoCuenta tipoCuentaSeleccionada;
    private int idTipoCuentaSeleccionada;    
    
    public CuentaManagedBean() {
    }
    
    @PostConstruct
    public void init(){
        cuentas = cuentaFacade.findAll();
        tiposCuenta = tipoCuentaFacade.findAll();
    }

    public Integer getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Integer idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getNombreUsuarioCuenta() {
        return nombreUsuarioCuenta;
    }

    public void setNombreUsuarioCuenta(String nombreUsuarioCuenta) {
        this.nombreUsuarioCuenta = nombreUsuarioCuenta;
    }

    public String getEmailCuenta() {
        return emailCuenta;
    }

    public void setEmailCuenta(String emailCuenta) {
        this.emailCuenta = emailCuenta;
    }

    public String getKeyCuenta() {
        return keyCuenta;
    }

    public void setKeyCuenta(String keyCuenta) {
        this.keyCuenta = keyCuenta;
    }

    public String getSecretCuenta() {
        return secretCuenta;
    }

    public void setSecretCuenta(String secretCuenta) {
        this.secretCuenta = secretCuenta;
    }

    public String getTokenCuenta() {
        return tokenCuenta;
    }

    public void setTokenCuenta(String tokenCuenta) {
        this.tokenCuenta = tokenCuenta;
    }

    public List<Equipo> getEquipoList() {
        return equipoList;
    }

    public void setEquipoList(List<Equipo> equipoList) {
        this.equipoList = equipoList;
    }

    public TipoCuenta getIdTipoCuenta() {
        return idTipoCuenta;
    }

    public void setIdTipoCuenta(TipoCuenta idTipoCuenta) {
        this.idTipoCuenta = idTipoCuenta;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<Cuenta> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<Cuenta> cuentas) {
        this.cuentas = cuentas;
    }

    public List<Cuenta> getCuentasFiltradas() {
        return cuentasFiltradas;
    }

    public void setCuentasFiltradas(List<Cuenta> cuentasFiltradas) {
        this.cuentasFiltradas = cuentasFiltradas;
    }

    public Cuenta getCuentaSeleccionada() {
        return cuentaSeleccionada;
    }

    public void setCuentaSeleccionada(Cuenta cuentaSeleccionada) {
        this.cuentaSeleccionada = cuentaSeleccionada;
    }

    public TipoCuenta getTipoCuentaSeleccionada() {
        return tipoCuentaSeleccionada;
    }

    public void setTipoCuentaSeleccionada(TipoCuenta tipoCuentaSeleccionada) {
        this.tipoCuentaSeleccionada = tipoCuentaSeleccionada;
    }

    public int getIdTipoCuentaSeleccionada() {
        return idTipoCuentaSeleccionada;
    }

    public void setIdTipoCuentaSeleccionada(int idTipoCuentaSeleccionada) {
        this.idTipoCuentaSeleccionada = idTipoCuentaSeleccionada;
    }

    public List<TipoCuenta> getTiposCuenta() {
        return tiposCuenta;
    }

    public void setTiposCuenta(List<TipoCuenta> tiposCuenta) {
        this.tiposCuenta = tiposCuenta;
    }
    
    public void nuevaCuenta(){
        TipoCuenta tipoC = tipoCuentaFacade.buscarPorId(idTipoCuentaSeleccionada);
        Usuario usr = usuarioFacade.buscarPorLogin("fguzman");
        Cuenta cuenta = new Cuenta(nombreUsuarioCuenta, emailCuenta, keyCuenta, secretCuenta, tokenCuenta, tipoC, usr);
        
        if("Trello".equals(tipoC.getNombreTipoCuenta())){
            if(tableroTrello.checkCuenta(cuenta)){
                cuentaFacade.create(cuenta);
                FacesMessage msg = new FacesMessage("Cuenta Agregada",tipoC.getNombreTipoCuenta());  
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }else{
                FacesMessage msg = new FacesMessage("Cuenta No Agregada","ERROR: Datos de cuenta incorrectos");  
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }else{
            if(tableroKanbanize.checkCuenta(cuenta)){
               cuentaFacade.create(cuenta);
               FacesMessage msg = new FacesMessage("Cuenta Agregada",tipoC.getNombreTipoCuenta());  
               FacesContext.getCurrentInstance().addMessage(null, msg); 
            }else{
                FacesMessage msg = new FacesMessage("Cuenta No Agregada","ERROR: Datos de cuenta incorrectos");  
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }
        limpiar();
        cuentas = cuentaFacade.findAll();
    }
    
    public void onEdit(RowEditEvent event){
        Cuenta cuenta = (Cuenta) event.getObject();
        cuentaFacade.edit(cuenta);
        FacesMessage msg = new FacesMessage("Cuenta Editada","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onCancel(RowEditEvent event){
        FacesMessage msg = new FacesMessage("No se realizó ningun cambio","");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void eliminarAsignatura(){
        try {
            cuentaFacade.remove(cuentaSeleccionada);
            FacesMessage msg = new FacesMessage("Cuenta Eliminada","");        
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage("Cuenta NO Eliminada","ERROR: Esta cuenta depende de otros elementos.");        
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        cuentas = cuentaFacade.findAll();
    }
    
    public void limpiar(){
        idCuenta = null;
        nombreUsuarioCuenta = null;
        emailCuenta = null;
        keyCuenta = null;
        secretCuenta = null;
        tokenCuenta = null;
        idTipoCuenta = null;
        idTipoCuentaSeleccionada = 0;
    }
}
