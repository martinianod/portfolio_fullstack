package com.martiniano.crm.service;

import com.martiniano.crm.dto.ClientRequest;
import com.martiniano.crm.entity.Client;
import com.martiniano.crm.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ActivityService activityService;

    public ClientService(ClientRepository clientRepository,
                         ActivityService activityService) {
        this.clientRepository = clientRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Client createClient(ClientRequest request) {
        Client client = new Client();
        client.setName(request.getName());
        client.setCompany(request.getCompany());
        client.setPrimaryContactName(request.getPrimaryContactName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setNotes(request.getNotes());
        client.setTags(request.getTags());
        
        if (request.getStatus() != null) {
            client.setStatus(request.getStatus());
        } else {
            client.setStatus("ACTIVE");
        }

        Client savedClient = clientRepository.save(client);

        activityService.logActivity("CLIENT", savedClient.getId(), "CREATED", 
                "Client created", null, null);

        return savedClient;
    }

    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> searchClients(String search, Pageable pageable) {
        return clientRepository.searchClients(search, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> getClientsByStatus(String status, Pageable pageable) {
        return clientRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Client updateClient(Long id, ClientRequest request) {
        Client client = getClientById(id);

        if (request.getName() != null) {
            client.setName(request.getName());
        }
        if (request.getCompany() != null) {
            client.setCompany(request.getCompany());
        }
        if (request.getPrimaryContactName() != null) {
            client.setPrimaryContactName(request.getPrimaryContactName());
        }
        if (request.getEmail() != null) {
            client.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            client.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            client.setAddress(request.getAddress());
        }
        if (request.getNotes() != null) {
            client.setNotes(request.getNotes());
        }
        if (request.getTags() != null) {
            client.setTags(request.getTags());
        }
        if (request.getStatus() != null) {
            client.setStatus(request.getStatus());
        }

        Client updatedClient = clientRepository.save(client);

        activityService.logActivity("CLIENT", client.getId(), "UPDATED", 
                "Client information updated", null, null);

        return updatedClient;
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        clientRepository.delete(client);
        
        activityService.logActivity("CLIENT", id, "DELETED", "Client deleted", null, null);
    }
}
