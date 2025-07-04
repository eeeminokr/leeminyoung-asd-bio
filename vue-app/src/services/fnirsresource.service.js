import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class FnirsResourceService {
    download(subjectId, projectSeq, trialIndex) {
        const options = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const url = apiPaths.buildPath(`/v1/fnirsresource/download/${subjectId}/${projectSeq}/${trialIndex}`);
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
        const url = apiPaths.buildPath(`/v1/fnirsresource/dataset/download/${projectSeq}`);
        console.log("service.js[;url] - " + url)
        return httpClient.get(url, options);
    }


    postFnirsResource(subjectId, projectSeq, trialIndex) {
        const data = {
            subjectId: subjectId,
            projectSeq: projectSeq,
            trialIndex: trialIndex

        };
        console.log(data)
        const url = apiPaths.buildPath(`/v1/fnirsresource/${projectSeq}/${trialIndex}/remove/${subjectId}/`);
        return httpClient.post(url,data);

    }

}

export default new FnirsResourceService();