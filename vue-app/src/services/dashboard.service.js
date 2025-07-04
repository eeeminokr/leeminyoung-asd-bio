import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class DashboardService {

    getProjectdDataState() {
        const url = apiPaths.buildPath(`/v1/dashboard/data-state`);
        return httpClient.get(url);
    }
    getDataSummarybyOrgId(id) {
        const url = apiPaths.buildPath(`/v1/dashboard/data-state/${id}`);
        return httpClient.get(url);
    }
    getTrialInfoStateSurmmary(index) {
        const url = apiPaths.buildPath(`/v1/dashboard/trialdata-state/${index}`);
        return httpClient.get(url);
    }

}

export default new DashboardService();