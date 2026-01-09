package com.tavinki.taskiu.modules.turnstile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO for Cloudflare Turnstile verification response.
 * Maps the JSON response from Cloudflare's Turnstile API.
 * 
 * @see <a href=
 *      "https://developers.cloudflare.com/turnstile/get-started/server-side-validate/">Cloudflare
 *      Turnstile Documentation</a>
 * @param success     Indicates if the verification was successful.
 * @param challengeTs Timestamp of the challenge.
 * @param hostname    The hostname of the site where the challenge was solved.
 * @param errorCodes  List of error codes if the verification failed.
 */
@Data
public class TurnstileResponse {
    private boolean success;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;
}
