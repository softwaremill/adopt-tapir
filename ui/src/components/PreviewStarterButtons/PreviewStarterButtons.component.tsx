import {Button} from "@mui/material";
import {useCallback} from "react";
import {doRequestStarter, StarterRequest} from "api/starter";
import {useNavigate} from "react-router-dom";

type Props = {
  request?: StarterRequest,
  apiCaller: (call: () => Promise<void>) => void
}

export function PreviewStarterButtons({request, apiCaller}: Props) {
  const navigate = useNavigate();

  const handleBack = useCallback(() => {
    navigate('/', {state: request});
  }, [request]);

  const handleGenerateZip = useCallback(() => {
    if (request !== undefined) {
      apiCaller(() => doRequestStarter(request))
    } else {
      throw Error(`Object ${request} should be populated by this point.`)
    }
  }, [request]);
  return (<>
    <Button
      onClick={handleBack}
      variant="contained"
      color="secondary"
      size="medium"
      disableElevation>
      Back
    </Button>
    <Button
      onClick={handleGenerateZip}
      variant="contained"
      color="primary"
      size="medium"
      type="submit"
      disableElevation>GENERATE .ZIP</Button>
  </>);
}
