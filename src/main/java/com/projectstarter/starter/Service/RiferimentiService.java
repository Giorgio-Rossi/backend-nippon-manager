package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.RiferimentiResponse;
import com.projectstarter.starter.Dto.Response.RiferimentiResponse.Voce;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Entity.Presenza;
import com.projectstarter.starter.Util.Giorni;
import com.projectstarter.starter.Util.Periodi;
import com.projectstarter.starter.Util.Statistiche;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Compone le voci di dominio a partire dagli enum, senza duplicarne le etichette. */
@Service
public class RiferimentiService {

    public RiferimentiResponse riferimenti() {
        RiferimentiResponse response = new RiferimentiResponse();
        response.setGiorni(giorni());
        response.setTipiPagamento(Arrays.stream(Pagamento.Tipo.values())
                .map(tipo -> Voce.di(tipo.name(), tipo.getLabel(), tipo.getBreve()))
                .toList());
        response.setMetodiPagamento(Arrays.stream(Pagamento.Metodo.values())
                .map(metodo -> Voce.di(metodo.name(), metodo.getLabel()))
                .toList());
        response.setStatiPresenza(Arrays.stream(Presenza.Stato.values())
                .map(stato -> Voce.di(stato.name(), stato.getLabel(), stato.getSigla()))
                .toList());
        response.setPeriodiStatistiche(Periodi.preset().stream()
                .map(preset -> Voce.di(preset.name(), preset.getLabel()))
                .toList());
        response.setOrdinamentiClassifica(Arrays.stream(Statistiche.Ordine.values())
                .map(ordine -> Voce.di(ordine.name(), ordine.getLabel()))
                .toList());
        return response;
    }

    /** I giorni ISO, da lunedi a domenica. */
    private List<Voce> giorni() {
        String[] nomi = Giorni.nomi();
        String[] brevi = Giorni.brevi();
        List<Voce> voci = new ArrayList<>(nomi.length);
        for (int i = 0; i < nomi.length; i++) {
            voci.add(Voce.di(String.valueOf(i + 1), nomi[i], brevi[i]));
        }
        return voci;
    }
}
