package com.constitution.backend.config;

import com.constitution.backend.entity.*;
import com.constitution.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final QuizQuestionRepository quizRepository;
    private final ForumThreadRepository threadRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding demo data...");

        // ── Users ────────────────────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .fullName("Admin User").email("admin@const.in").phone("9000000001")
                .password(passwordEncoder.encode("Admin@123"))
                .role(User.Role.admin).verified(true).active(true).build());

        User citizen = userRepository.save(User.builder()
                .fullName("Arjun Sharma").email("citizen@const.in").phone("9000000002")
                .password(passwordEncoder.encode("Citizen@123"))
                .role(User.Role.citizen).verified(true).active(true).build());

        User educator = userRepository.save(User.builder()
                .fullName("Priya Menon").email("edu@const.in").phone("9000000003")
                .password(passwordEncoder.encode("Edu@123A"))
                .role(User.Role.educator).verified(true).active(true).build());

        User legal = userRepository.save(User.builder()
                .fullName("Adv. Rao").email("legal@const.in").phone("9000000004")
                .password(passwordEncoder.encode("Legal@123"))
                .role(User.Role.legal_expert).verified(true).active(true).build());

        // ── Articles ─────────────────────────────────────────────────────────
        List<Article> articles = articleRepository.saveAll(List.of(
                Article.builder().part("Part I").title("Article 1 — Name and Territory of the Union")
                        .summary("India, that is Bharat, shall be a Union of States.")
                        .tags("Union,Territory").status(Article.Status.Published).author(educator).build(),

                Article.builder().part("Part III").title("Article 14 — Right to Equality")
                        .summary("The State shall not deny to any person equality before the law.")
                        .tags("Fundamental Rights,Equality").status(Article.Status.Published).author(legal).build(),

                Article.builder().part("Part III").title("Article 19 — Freedom of Speech")
                        .summary("All citizens shall have the right to freedom of speech and expression.")
                        .tags("Fundamental Rights,Freedom").status(Article.Status.Published).author(educator).build(),

                Article.builder().part("Part III").title("Article 21 — Right to Life")
                        .summary("No person shall be deprived of his life or personal liberty.")
                        .tags("Fundamental Rights,Life,Liberty").status(Article.Status.Published).author(legal).build(),

                Article.builder().part("Part III").title("Article 21A — Right to Education")
                        .summary("The State shall provide free and compulsory education to all children aged 6–14.")
                        .tags("Fundamental Rights,Education").status(Article.Status.Published).author(educator).build(),

                Article.builder().part("Part IV-A").title("Article 51A — Fundamental Duties")
                        .summary("It shall be the duty of every citizen to abide by the Constitution.")
                        .tags("Duties,Citizens").status(Article.Status.Draft).author(admin).build()
        ));

        // ── Quiz Questions ────────────────────────────────────────────────────
        quizRepository.saveAll(List.of(
                QuizQuestion.builder()
                        .question("What does the word \"Sovereign\" in the Preamble mean?")
                        .optionA("India is supreme").optionB("India is democratic")
                        .optionC("India is not dependent on any outside power").optionD("India has a single ruler")
                        .correctOption(2).category("Preamble").createdBy(educator).build(),

                QuizQuestion.builder()
                        .question("Which Article guarantees Right to Equality?")
                        .optionA("Article 12").optionB("Article 14")
                        .optionC("Article 19").optionD("Article 21")
                        .correctOption(1).category("Fundamental Rights").createdBy(educator).build(),

                QuizQuestion.builder()
                        .question("The Right to Education was added by which Amendment?")
                        .optionA("42nd").optionB("86th").optionC("44th").optionD("91st")
                        .correctOption(1).category("Amendments").createdBy(legal).build(),

                QuizQuestion.builder()
                        .question("Article 21 deals with:")
                        .optionA("Right to Vote").optionB("Right to Property")
                        .optionC("Right to Life and Personal Liberty").optionD("Right to Education")
                        .correctOption(2).category("Fundamental Rights").createdBy(educator).build(),

                QuizQuestion.builder()
                        .question("Which case established the Basic Structure Doctrine?")
                        .optionA("Maneka Gandhi vs Union of India")
                        .optionB("Kesavananda Bharati vs State of Kerala")
                        .optionC("Golaknath vs State of Punjab")
                        .optionD("Minerva Mills vs Union of India")
                        .correctOption(1).category("Landmark Cases").createdBy(legal).build()
        ));

        // ── Forum Threads ─────────────────────────────────────────────────────
        threadRepository.saveAll(List.of(
                ForumThread.builder().topic("Preamble")
                        .title("Does \"Socialist\" in the Preamble restrict economic freedom?")
                        .body("A detailed discussion on the implications of the word Socialist in the Indian Constitution...")
                        .author(citizen).likes(32).build(),

                ForumThread.builder().topic("Rights")
                        .title("Is Freedom of Speech absolute under Article 19?")
                        .body("Article 19 grants freedom of speech but also lists reasonable restrictions. Where do we draw the line?")
                        .author(educator).likes(67).build(),

                ForumThread.builder().topic("Duties")
                        .title("How should Fundamental Duties be enforced?")
                        .body("Part IVA outlines duties but there is no enforcement mechanism. Should there be?")
                        .author(legal).likes(18).build()
        ));

        log.info("Demo seed complete: 4 users | {} articles | 5 quiz questions | 3 threads",
                articles.size());
    }
}
