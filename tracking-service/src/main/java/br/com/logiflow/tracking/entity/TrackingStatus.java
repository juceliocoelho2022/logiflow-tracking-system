package br.com.logiflow.tracking.entity;

public enum TrackingStatus {
    PEDIDO_CRIADO,
    PAGAMENTO_APROVADO,
    ESTOQUE_RESERVADO,
    EM_SEPARACAO,
    EXPEDIDO,
    EM_TRANSPORTE,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    ENTREGA_NAO_REALIZADA,
    CANCELADO
}
