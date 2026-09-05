package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.DashboardResponse;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Util.Certificati;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Riepilogo della home: conteggi e certificati in scadenza. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AtletaRepository atletaRepository;
    private final CorsoRepository corsoRepository;
    private final AtletaService atletaService;

    /**
     * @param giorniPreavviso finestra dei certificati in scadenza; il valore
     *                        predefinito e la soglia della politica societaria
     */
    public DashboardResponse riepilogo(Integer giorniPreavviso) {
        int giorni = giorniPreavviso != null ? giorniPreavviso : Certificati.GIORNI_PREAVVISO;
        List<AtletaResponse> certificati = atletaService.findExpiringCertificates(giorni, true);

        DashboardResponse response = new DashboardResponse();
        response.setAtletiTotali(atletaRepository.count());
        response.setAtletiAttivi(atletaRepository.countByAttivoTrue());
        response.setCorsiAttivi(corsoRepository.countByAttivoTrue());
        response.setGiorniPreavviso(giorni);
        response.setCertificatiInScadenza(certificati.size());
        response.setCertificati(certificati);
        return response;
    }
}
