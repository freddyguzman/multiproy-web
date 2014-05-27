/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.managedbeans;

import cl.usach.entities.Equipo;
import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.EquipoFacadeLocal;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import cl.usach.trellosessionbeans.ListaTrelloLocal;
import cl.usach.trellosessionbeans.MiembroTrelloLocal;
import cl.usach.trellosessionbeans.TarjetaTrelloLocal;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author FGT
 */
@Named(value = "escritorioManagedBean")
@ViewScoped
public class EscritorioManagedBean {
    @EJB
    private MiembroTrelloLocal miembroTrello;
    @EJB
    private TarjetaTrelloLocal tarjetaTrello;
    @EJB
    private ListaTrelloLocal listaTrello;
    @EJB
    private EquipoFacadeLocal equipoFacade;
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    private final SesionManagedBean sesionManagedBean = new SesionManagedBean();
    
    private final String loginUsuario = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    
    public EscritorioManagedBean() {
    }
    
    public void buscarDatos(){
        Usuario usuario = usuarioFacade.buscarPorLogin(loginUsuario);
        List<Equipo> equipos = equipoFacade.buscarPorUsuario(usuario);
        
        for (Equipo equipo : equipos) {
            if("Trello".equals(equipo.getIdCuenta().getIdTipoCuenta().getNombreTipoCuenta())){                
                //Obtener o Actualizar Miembros
                miembroTrello.buscarMiembros(equipo);
                
                //Obtener o Actualizar Listas
                listaTrello.buscarListaPorTablero(equipo);              
                       
                //Obtener o Actualizar Tarjetas
                tarjetaTrello.buscarTarjetasPorLista(equipo);
            }
            
        }
    }
    
}
