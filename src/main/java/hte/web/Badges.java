package hte.web;

import hte.jpa.CandidacyJPA;
import hte.jpa.JpaUtil;
import hte.jpa.ResponseJPA;
import hte.jpa.TagJPA;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("badges")
@Produces(MediaType.APPLICATION_JSON)
public class Badges {

    @Path("{username}")
    @GET
    public Badge getMyBadges(@PathParam("username") String username) {

        List<ResponseJPA> myResponse = JpaUtil.getEntityManager()
                .createQuery("from ResponseJPA where username = :username", ResponseJPA.class)
                .setParameter("username", username)
                .getResultList();

        Map<CandidacyJPA, Score> candidacyScoreMap = new HashMap<CandidacyJPA, Score>();
        Map<TagJPA, Score> tagScoreMap = new HashMap<TagJPA, Score>();
        for (ResponseJPA response : myResponse) {
            CandidacyJPA candidacy = response.question.candidacy;
            TagJPA tagLevel1 = response.question.tagLevel1;

            Score scoreCandidat = candidacyScoreMap.get(candidacy);
            if (scoreCandidat == null) {
                scoreCandidat = new Score(1, response.correct ? 1 : 0);
                candidacyScoreMap.put(candidacy, scoreCandidat);
            } else {
                scoreCandidat.answered++;
                if (response.correct) {
                    scoreCandidat.rights++;
                }
            }

            Score scoreTag = tagScoreMap.get(tagLevel1);
            if (scoreTag == null) {
                scoreTag = new Score(1, response.correct ? 1 : 0);
                tagScoreMap.put(tagLevel1, scoreTag);
            } else {
                scoreTag.answered++;
                if (response.correct) {
                    scoreTag.rights++;
                }
            }
        }

        List<BadgeCandidat> badgesCandidats = new ArrayList<BadgeCandidat>();
        for (Map.Entry<CandidacyJPA, Score> entry : candidacyScoreMap.entrySet()) {
            badgesCandidats.add(new BadgeCandidat(entry.getKey(), entry.getValue().answered, entry.getValue().rights));
        }

        List<BadgeTheme> badgeThemes = new ArrayList<BadgeTheme>();
        for (Map.Entry<TagJPA, Score> entry : tagScoreMap.entrySet()) {
            badgeThemes.add(new BadgeTheme(entry.getKey(), entry.getValue().answered, entry.getValue().rights));
        }

        return new Badge(badgesCandidats, badgeThemes);
    }

    public static class Badge {
        public List<BadgeCandidat> badgesCandidats;
        public List<BadgeTheme> badgeThemes;

        public Badge(List<BadgeCandidat> badgesCandidats, List<BadgeTheme> badgeThemes) {
            this.badgesCandidats = badgesCandidats;
            this.badgeThemes = badgeThemes;
        }
    }

    public static class BadgeCandidat {
        public CandidacyJPA candidacy;
        public int answered;
        public int rights;

        public BadgeCandidat(CandidacyJPA candidacy, int answered, int rights) {
            this.candidacy = candidacy;
            this.answered = answered;
            this.rights = rights;
        }
    }

    public static class BadgeTheme {
        public TagJPA theme;
        public int answered;
        public int rights;

        public BadgeTheme(TagJPA theme, int answered, int rights) {
            this.theme = theme;
            this.answered = answered;
            this.rights = rights;
        }
    }

    public static class Score {
        public int answered;
        public int rights;

        public Score(int answered, int rights) {
            this.answered = answered;
            this.rights = rights;
        }
    }
}