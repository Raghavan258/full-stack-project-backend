package com.constitution.backend.service;

import com.constitution.backend.dto.QueryAnswerRequest;
import com.constitution.backend.dto.QueryRequest;
import com.constitution.backend.entity.Query;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.QueryRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final QueryRepository queryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Query createQuery(QueryRequest req, String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Query.QueryBuilder builder = Query.builder()
                .question(req.getQuestion())
                .citizen(citizen)
                .status(Query.QueryStatus.PENDING);

        if (req.getAssignedToId() != null) {
            User assignee = userRepository.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ApiException("Assignee not found", HttpStatus.NOT_FOUND));
            builder.assignedTo(assignee).status(Query.QueryStatus.ASSIGNED);
        }

        return queryRepository.save(builder.build());
    }

    public List<Query> getMyCitizenQueries(String email) {
        User citizen = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return queryRepository.findByCitizen(citizen);
    }

    public List<Query> getForResponder(String email) {
        User responder = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return queryRepository.findByAssignedToOrStatus(responder, Query.QueryStatus.PENDING);
    }

    public List<Query> getAllQueries() {
        return queryRepository.findAll();
    }

    @Transactional
    public Query assignQuery(Long queryId, Long userId) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ApiException("Query not found", HttpStatus.NOT_FOUND));
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        query.setAssignedTo(assignee);
        query.setStatus(Query.QueryStatus.ASSIGNED);
        return queryRepository.save(query);
    }

    @Transactional
    public Query answerQuery(Long queryId, QueryAnswerRequest req, String responderEmail) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ApiException("Query not found", HttpStatus.NOT_FOUND));
        User responder = userRepository.findByEmail(responderEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        query.setAnswer(req.getAnswer());
        query.setAnsweredBy(responder);
        query.setStatus(Query.QueryStatus.ANSWERED);
        return queryRepository.save(query);
    }

    @Transactional
    public Query closeQuery(Long queryId) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ApiException("Query not found", HttpStatus.NOT_FOUND));
        query.setStatus(Query.QueryStatus.CLOSED);
        return queryRepository.save(query);
    }

    @Transactional
    public void deleteQuery(Long queryId) {
        if (!queryRepository.existsById(queryId)) {
            throw new ApiException("Query not found", HttpStatus.NOT_FOUND);
        }
        queryRepository.deleteById(queryId);
    }
}
