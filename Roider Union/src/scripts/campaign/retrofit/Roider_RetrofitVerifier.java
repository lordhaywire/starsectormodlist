package scripts.campaign.retrofit;

import com.fs.starfarer.api.campaign.RepLevel;

/**
 * Author: SafariJohn
 */
public interface Roider_RetrofitVerifier {
    public Roider_RetrofitsKeeper.RetrofitData verifyData(String id, String fitter,
                String source, String target, double cost,
                double time, RepLevel rep, boolean commission);
}
