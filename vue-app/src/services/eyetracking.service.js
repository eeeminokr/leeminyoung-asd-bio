import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class EyetrackingService {
    download(subjectId, projectSeq, trialIndex) {
        const options = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const url = apiPaths.buildPath(`/v1/eyetracking/download/${subjectId}/${projectSeq}/${trialIndex}`);
        console.log("service.js[;url] - " + url)
        return httpClient.get(url, options);
    }

    dataSetDownload(projectSeq) {
        const options = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const url = apiPaths.buildPath(`/v1/eyetracking/dataset/download/${projectSeq}`);
        console.log("service.js[;url] - " + url)
        return httpClient.get(url, options);
    }


    postDeleteEyeTracking(subjectId, projectSeq, trialIndex, categories) {
        const data = {
            subjectId: subjectId,
            projectSeq: projectSeq,
            trialIndex: trialIndex,
            types: categories
        };
        console.log(data)
        const url = apiPaths.buildPath(`/v1/eyetracking/delete`);
        return httpClient.post(url, data);

    }

}

export default new EyetrackingService();