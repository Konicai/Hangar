package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.ReviewAction;
import io.papermc.hangar.model.common.projects.ReviewState;
import io.papermc.hangar.model.internal.api.requests.versions.ReviewMessage;
import io.papermc.hangar.model.internal.versions.HangarReview;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.versions.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@Controller
@Secured("ROLE_USER")
@RateLimit(path = "review")
@RequestMapping(path = "/api/internal/reviews")
@PermissionRequired(NamedPermission.REVIEWER)
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping(path = "/{versionId}/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarReview>> getVersionReviews(@PathVariable long versionId) {
        return ResponseEntity.ok(reviewService.getHangarReviews(versionId));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startVersionReview(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.startReview(versionId, message);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addVersionReviewMessage(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.addReviewMessage(versionId, message);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/stop", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void stopVersionReview(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.stopReview(versionId, message);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/reopen", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void reopenVersionReview(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.reopenReview(versionId, message, ReviewAction.REOPEN);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/approve", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void approveVersionReview(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.approveReview(versionId, message, ReviewState.REVIEWED, ReviewAction.APPROVE);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/approvePartial", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void approvePartialVersionReview(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.approveReview(versionId, message, ReviewState.PARTIALLY_REVIEWED, ReviewAction.PARTIALLY_APPROVE);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{versionId}/reviews/undoApproval", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void undoApproval(@PathVariable long versionId, @Valid @RequestBody ReviewMessage message) {
        reviewService.undoApproval(versionId, message);
    }
}
