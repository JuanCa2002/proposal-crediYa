package co.com.pragma.usecase.proposal.constants;

public class ProposalMessageConstants {

    public static final String PROPOSAL_TYPE_NOT_FOUND = "No se encontró un tipo de solicitud con el id {0}";
    public static final String STATE_NOT_FOUND = "No se encontró un estado con el id {0}";
    public static final String INITIAL_STATE_NOT_FOUND = "No se puede crear la solicitud ya que no se encontro el estado inicial con nombre {0}";
    public static final String PROPOSAL_TYPE_NOT_MATCH = "Para el tipo de solicitud {0}, el valor tiene que estar en un rango de ({1} - {2})";
    public static final String USER_NOT_MATCH_LOGIN_USER = "El usuario que realiza la solicitud no es el mismo que esta logeado";
    public static final String PROPOSAL_BY_ID_NOT_FOUND_EXCEPTION = "No se encontro una solicitud con id {0}";
    public static final String PROPOSAL_STATE_CAN_NOT_BE_CHANGE = "No se puede cambiar el estado de la solicitud una vez esta APROBADA O RECHAZADA";
    public static final String PROPOSAL_STATE_ALREADY_IS_THE_SELECTED_ONE = "El estado de la solicitud ya es {0}";
    public static final String STATE_BY_NAME_NOT_FOUND_EXCEPTION = "No se encontró un estado con el nombre {0}";
}
