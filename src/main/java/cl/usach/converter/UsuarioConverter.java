/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cl.usach.converter;

import cl.usach.entities.Usuario;
import cl.usach.sessionbeans.UsuarioFacadeLocal;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author FGT
 */
public class UsuarioConverter implements Converter{
        
    @EJB
    private UsuarioFacadeLocal usuarioFacade;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return usuarioFacade.buscarPorId(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Usuario) value).getIdUsuario().toString();
    }
}
