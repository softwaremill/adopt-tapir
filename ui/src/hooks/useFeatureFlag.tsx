export const useFeatureFlag = () => {
  return {
    isScalaVersionFieldVisible: JSON.parse(process.env.REACT_APP_SCALA_VERSION_FF ?? ''),
  };
};
